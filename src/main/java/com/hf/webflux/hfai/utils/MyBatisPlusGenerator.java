package com.hf.webflux.hfai.utils;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MyBatisPlusGenerator {
    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/mall", "root", "1q2w3e4r")
                // 全局配置
                .globalConfig((scanner, builder) -> {
                    //设置注释信息-作者
                    builder.author(scanner.apply("请输入作者名称？"));
                    //设置swagger注解
                    //builder.enableSwagger();
                    //设置代码生成的路径
                    builder.outputDir(System.getProperty("user.dir")+"/src/main/java");
                })
                // 包配置
                .packageConfig((scanner, builder) -> {
                    builder.parent(scanner.apply("请输入包名？"))
                            //设置xml文件路径
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir")+"/src/main/resources/mapper"));
                })
                // 策略配置
                .strategyConfig((scanner, builder) -> {
                    builder.addInclude(MyBatisPlusGenerator.getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                            .controllerBuilder().enableRestStyle().enableHyphenStyle().build();
                    builder.serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImp")
                            .build();
                    //entity的策略配置
                    builder.entityBuilder()
                            //启用Lombok插件
                            .enableLombok()
                            .enableTableFieldAnnotation()

                            .versionColumnName("version")
                            .logicDeleteColumnName("is_delete")
                            //设置字段名的命名策略为下划线转驼峰命名
                            .columnNaming(NamingStrategy.underline_to_camel)
                            //主键策略递增
                            .idType(IdType.AUTO)
                            .formatFileName("%s")
                            .build();
                })
                .execute();
    }
}

