module t.winfileshare {
    requires javafx.graphics;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires static lombok;
    requires spring.webmvc;
    requires jakarta.persistence;
    requires google.api.client;
    requires spring.beans;
    requires javafx.fxml;
    requires javafx.controls;
    requires jakarta.annotation;
    requires com.google.api.client.auth;
    requires spring.web;
    requires org.apache.tomcat.embed.core;
    requires spring.data.jpa;
    requires spring.tx;
    requires zip4j;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.services.drive;
    requires spring.core;
    requires thymeleaf;
    requires org.apache.logging.log4j;
    requires org.hibernate.orm.core;
    requires spring.aop;
    requires spring.boot.starter.aop;

    exports com.example.twinfileshare;
    exports com.example.twinfileshare.service;
    exports com.example.twinfileshare.service.utility;
    exports com.example.twinfileshare.listener;
    exports com.example.twinfileshare.mvc.controller;

    opens com.example.twinfileshare;
    opens com.example.twinfileshare.service;
    opens com.example.twinfileshare.config;
    opens com.example.twinfileshare.entity;
    opens com.example.twinfileshare.listener;
    opens com.example.twinfileshare.mvc.controller;
    opens com.example.twinfileshare.fx;
    opens com.example.twinfileshare.fx.view;
    opens com.example.twinfileshare.fx.model;
    opens com.example.twinfileshare.fx.alert;
}