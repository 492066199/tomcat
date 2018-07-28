package com.test;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            //1、创建Digester对象实例
            Digester digester = new Digester();

            //2、配置属性值
            digester.setValidating(false);

            //3、push对象到对象栈
            digester.push(new Foo());

            //4、设置匹配模式、规则
//            digester.addObjectCreate("foo", "com.test.Foo");
            digester.addSetProperties("foo");
            digester.addObjectCreate("foo/bar", "com.test.Bar");
            digester.addSetProperties("foo/bar");
            digester.addSetNext("foo/bar", "addBar", "com.test.Bar");

            //5、开始解析
            Foo foo = (Foo) digester.parse(Main.class.getClassLoader().getResourceAsStream("test.xml"));

            //6、打印解析结果
            System.out.println(foo.getName());
            for (Bar bar : foo.getBarList()) {
                System.out.println(bar.getId() + "," + bar.getTitle());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
