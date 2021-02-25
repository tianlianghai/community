package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstance;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstance {
    private  static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private  String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {

        return "/site/register";
    }

    @RequestMapping(path = "register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map= userService.register(user);
        if (map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了激活邮件，请尽快几号");
            model.addAttribute("target","/index");
            return "site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String login(){
        return ("site/login");
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result= userService.activation(userId,code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功，将跳转到登陆页面");
            model.addAttribute("target","/login");
        } else if (result==ACTIVATION_REPEAT) {
            model.addAttribute("msg","操作无效，重复激活");
            model.addAttribute("target","/index");
        } else {
            model.addAttribute("msg","激活失败，您的激活码不正确");
            model.addAttribute("target","/index");
        }
        return "site/operate-result";
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response){
        //生成验证码
        String text=kaptchaProducer.createText();
        BufferedImage image=kaptchaProducer.createImage(text);

        //将验证码存入session
//        session.setAttribute("kaptcha",text);
        String kaptchaOwner= CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //将验证码存入Redis
        String kaptchaKey= RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);


        //将图片输出给浏览器
        response.setContentType("image/png");
        try (ServletOutputStream os = response.getOutputStream()) {
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("相应验证码失败 :"+e.getMessage());
        }
    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchaOwner")String kaptchaOwner){

        //检查验证码
        String kaptcha=null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha=(String) redisTemplate.opsForValue().get(redisKey);
        }

        //开发阶段跳过验证码,正式上线取消注释
//        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
//            model.addAttribute("codeMsg","验证码不正确");
//            return "/site/login";
//        }

        //检查账号密码
        int expiredSeconds=rememberMe?REMEMBER_ME_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map= userService.login(username,password,expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";   //redirect表示调用路径为"/index"的controller，而不是跳转到/index模板页面
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }

    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

}
