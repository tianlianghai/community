package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //根节点
    private TrieNode rootNode = new TrieNode();

    private static final String REPLACEMENT = "***";

    //在构造方法之后自动执行
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //将一个敏感词添加到前缀树
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            //指向子节点，进入下一轮循环
            tempNode = subNode;

            //设置结束表示
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }


        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        //指针1，开始指向root
        TrieNode tempNode = rootNode;

        //指针2，指向疑似敏感词开始
        int begin = 0;

        //指针3，指向疑似敏感词末尾
        int position = 0;

        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            //跳过符号
            if (isSymbol(c)) {
                //若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }

                //无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            //检查下级结点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                //以
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                //发现敏感词
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode=rootNode;
            }else {
                //检查下一个字符
                position++;
            }
        }

        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c) {
        //0x@E80~0x9FFF为东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //关键词查找树
    private class TrieNode {
        //关键词结束标志
        private boolean isKeywordEnd = false;

        //子节点
        private Map<Character, TrieNode> subNodes=new HashMap<>();


        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }
    }


}
