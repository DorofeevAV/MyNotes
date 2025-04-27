package com.dorofeev.mynotes.services;

// Класс локатор всех сервисов - сильно урезанная реализации DI
public class ServiceLocator {

    private static LoginService loginService;   // Сервис для работы с пользователями
    // Экземляр сервиса для работы с пользователями
    public static LoginService getLoginService() {
        if (loginService == null) {
            loginService = new LoginService();
        }
        return loginService;
    }
}