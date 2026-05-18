package com.contactmanager.util;

import java.util.*;

/**
 * 汉语拼音工具类（简化版）
 * 支持常用汉字转拼音，用于搜索功能
 * 注：完整版可集成 pinyin4j 库，此处内置常用字
 */
public class PinyinUtil {

    // 常用汉字拼音表（简化，涵盖常见姓名用字）
    private static final Map<Character, String> PINYIN_MAP = new HashMap<>();

    static {
        // 百家姓及常见姓名用字
        String[][] data = {
            {"赵","zhao"},{"钱","qian"},{"孙","sun"},{"李","li"},{"周","zhou"},
            {"吴","wu"},{"郑","zheng"},{"王","wang"},{"冯","feng"},{"陈","chen"},
            {"楚","chu"},{"卫","wei"},{"蒋","jiang"},{"沈","shen"},{"韩","han"},
            {"杨","yang"},{"朱","zhu"},{"秦","qin"},{"尤","you"},{"许","xu"},
            {"何","he"},{"吕","lv"},{"施","shi"},{"张","zhang"},{"孔","kong"},
            {"曹","cao"},{"严","yan"},{"华","hua"},{"金","jin"},{"魏","wei"},
            {"陶","tao"},{"姜","jiang"},{"戚","qi"},{"谢","xie"},{"邹","zou"},
            {"喻","yu"},{"柏","bai"},{"水","shui"},{"窦","dou"},{"章","zhang"},
            {"云","yun"},{"苏","su"},{"潘","pan"},{"葛","ge"},{"奚","xi"},
            {"范","fan"},{"彭","peng"},{"郎","lang"},{"鲁","lu"},{"韦","wei"},
            {"昌","chang"},{"马","ma"},{"苗","miao"},{"凤","feng"},{"花","hua"},
            {"方","fang"},{"俞","yu"},{"任","ren"},{"袁","yuan"},{"柳","liu"},
            {"酆","feng"},{"鲍","bao"},{"史","shi"},{"唐","tang"},{"费","fei"},
            {"廉","lian"},{"岑","cen"},{"薛","xue"},{"雷","lei"},{"贺","he"},
            {"倪","ni"},{"汤","tang"},{"滕","teng"},{"殷","yin"},{"罗","luo"},
            {"毕","bi"},{"郝","hao"},{"邬","wu"},{"安","an"},{"常","chang"},
            {"乐","le"},{"于","yu"},{"时","shi"},{"傅","fu"},{"皮","pi"},
            {"卞","bian"},{"齐","qi"},{"康","kang"},{"伍","wu"},{"余","yu"},
            {"元","yuan"},{"卜","bu"},{"顾","gu"},{"孟","meng"},{"平","ping"},
            {"黄","huang"},{"和","he"},{"穆","mu"},{"萧","xiao"},{"尹","yin"},
            {"姚","yao"},{"邵","shao"},{"湛","zhan"},{"汪","wang"},{"祁","qi"},
            {"毛","mao"},{"禹","yu"},{"狄","di"},{"米","mi"},{"贝","bei"},
            {"明","ming"},{"臧","zang"},{"计","ji"},{"伏","fu"},{"成","cheng"},
            {"戴","dai"},{"谈","tan"},{"宋","song"},{"茅","mao"},{"庞","pang"},
            {"熊","xiong"},{"纪","ji"},{"舒","shu"},{"屈","qu"},{"项","xiang"},
            {"祝","zhu"},{"董","dong"},{"梁","liang"},{"杜","du"},{"阮","ruan"},
            {"蓝","lan"},{"闵","min"},{"席","xi"},{"季","ji"},{"麻","ma"},
            {"强","qiang"},{"贾","jia"},{"路","lu"},{"娄","lou"},{"危","wei"},
            {"江","jiang"},{"童","tong"},{"颜","yan"},{"郭","guo"},{"梅","mei"},
            {"盛","sheng"},{"林","lin"},{"刁","diao"},{"钟","zhong"},{"徐","xu"},
            {"丘","qiu"},{"骆","luo"},{"高","gao"},{"夏","xia"},{"蔡","cai"},
            {"田","tian"},{"樊","fan"},{"胡","hu"},{"凌","ling"},{"霍","huo"},
            {"虞","yu"},{"万","wan"},{"支","zhi"},{"柯","ke"},{"昝","zan"},
            {"管","guan"},{"卢","lu"},{"莫","mo"},{"经","jing"},{"房","fang"},
            {"裘","qiu"},{"缪","miao"},{"干","gan"},{"解","xie"},{"应","ying"},
            {"宗","zong"},{"丁","ding"},{"宣","xuan"},{"贲","ben"},{"邓","deng"},
            {"郁","yu"},{"单","shan"},{"杭","hang"},{"洪","hong"},{"包","bao"},
            {"诸","zhu"},{"左","zuo"},{"石","shi"},{"崔","cui"},{"吉","ji"},
            {"钮","niu"},{"龚","gong"},{"程","cheng"},{"嵇","ji"},{"邢","xing"},
            // 常用名字用字
            {"伟","wei"},{"芳","fang"},{"娜","na"},{"敏","min"},{"静","jing"},
            {"丽","li"},{"强","qiang"},{"磊","lei"},{"洋","yang"},{"艳","yan"},
            {"勇","yong"},{"军","jun"},{"杰","jie"},{"娟","juan"},{"涛","tao"},
            {"超","chao"},{"秀","xiu"},{"霞","xia"},{"平","ping"},{"刚","gang"},
            {"桂","gui"},{"香","xiang"},{"兰","lan"},{"英","ying"},{"华","hua"},
            {"峰","feng"},{"龙","long"},{"飞","fei"},{"亮","liang"},{"鹏","peng"},
            {"辉","hui"},{"波","bo"},{"建","jian"},{"国","guo"},{"志","zhi"},
            {"文","wen"},{"健","jian"},{"海","hai"},{"燕","yan"},{"庆","qing"},
            {"明","ming"},{"春","chun"},{"红","hong"},{"小","xiao"},{"大","da"},
            {"中","zhong"},{"新","xin"},{"生","sheng"},{"信","xin"},{"美","mei"},
            {"月","yue"},{"光","guang"},{"天","tian"},{"彬","bin"},{"博","bo"},
            {"诚","cheng"},{"帅","shuai"},{"豪","hao"},{"睿","rui"},{"瑞","rui"},
            {"泽","ze"},{"宇","yu"},{"轩","xuan"},{"凯","kai"},{"浩","hao"},
            {"晨","chen"},{"阳","yang"},{"子","zi"},{"心","xin"},{"雨","yu"},
            {"雪","xue"},{"冰","bing"},{"清","qing"},{"婷","ting"},{"玲","ling"},
            {"萍","ping"},{"菊","ju"},{"莲","lian"},{"菁","jing"},{"雯","wen"},
            {"琳","lin"},{"珍","zhen"},{"珊","shan"},{"颖","ying"},{"欣","xin"},
        };
        for (String[] pair : data) {
            PINYIN_MAP.put(pair[0].charAt(0), pair[1]);
        }
    }

    /**
     * 将中文字符串转换为拼音全拼（小写，无声调）
     * 非中文字符保留原样
     */
    public static String toPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            String py = PINYIN_MAP.get(c);
            if (py != null) {
                sb.append(py);
            } else if (Character.isLetter(c)) {
                sb.append(c);
            }
            // 数字和标点跳过
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 获取拼音声母串（每个字的声母拼接）
     */
    public static String toPinyinInitials(String chinese) {
        if (chinese == null || chinese.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            String py = PINYIN_MAP.get(c);
            if (py != null && !py.isEmpty()) {
                // 处理复合声母 zh ch sh
                if (py.startsWith("zh") || py.startsWith("ch") || py.startsWith("sh")) {
                    sb.append(py.substring(0, 2));
                } else {
                    sb.append(py.charAt(0));
                }
            } else if (Character.isLetter(c)) {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 更新联系人的拼音字段
     */
    public static void updatePinyin(com.contactmanager.model.Contact contact) {
        if (contact.getName() != null) {
            contact.setPinyin(toPinyin(contact.getName()));
            contact.setPinyinInitials(toPinyinInitials(contact.getName()));
        }
    }
}
