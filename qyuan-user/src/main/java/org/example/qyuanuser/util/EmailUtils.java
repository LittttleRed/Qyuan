package org.example.qyuanuser.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class EmailUtils {
    // 定义机构邮箱域名后缀
    private static final Set<String> INSTITUTIONAL_DOMAINS = new HashSet<>(Arrays.asList(
        "edu.cn", "ac.cn", "edu", "ac.uk", "edu.au", "edu.in", "edu.sg", "edu.hk",
        "edu.tw", "edu.ru", "edu.fr", "edu.de", "edu.it", "edu.es", "edu.mx",
        "edu.br", "edu.jp", "edu.kr", "edu.za", "edu.ng", "edu.ar", "edu.co",
        "edu.eg", "edu.tr", "edu.sa", "edu.ua", "edu.pk", "edu.my", "edu.ph",
        "edu.vn", "edu.th", "edu.id", "edu.bd", "edu.lk", "edu.kz", "edu.ge",
        "edu.az", "edu.am", "edu.by", "edu.lv", "edu.lt", "edu.ee", "edu.pl",
        "edu.sk", "edu.cz", "edu.hu", "edu.ro", "edu.bg", "edu.si", "edu.hr",
        "edu.rs", "edu.ba", "edu.mk", "edu.al", "edu.me", "edu.xjtu.edu.cn",
        "pku.edu.cn", "tsinghua.edu.cn", "sjtu.edu.cn", "fudan.edu.cn",
        "zju.edu.cn", "nju.edu.cn", "ustc.edu.cn", "buaa.edu.cn", "bupt.edu.cn",
        "bit.edu.cn", "nankai.edu.cn", "tju.edu.cn", "cqu.edu.cn", "hhu.edu.cn"
    ));
    
    // 匹配edu.cn等模式的正则表达式
    private static final Pattern INSTITUTIONAL_EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.)?(edu|ac)(\\.[A-Za-z]{2})?$"
    );

    /**
     * 判断邮箱是否为机构邮箱
     * @param email 邮箱地址
     * @return 如果是机构邮箱返回true，否则返回false
     */
    public static boolean isInstitutionalEmail(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        
        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        
        // 检查是否在预定义的机构域名列表中
        if (INSTITUTIONAL_DOMAINS.contains(domain)) {
            return true;
        }
        
        // 检查是否匹配机构邮箱模式
        return INSTITUTIONAL_EMAIL_PATTERN.matcher(email).matches();
    }
}