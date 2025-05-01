package com.dorofeev.mynotes.services;

// Класс локатор всех сервисов - сильно урезанная реализации Dependency Injection
public class ServiceLocator {

    private static LoginService loginService;   // Сервис для работы с пользователями
    private static GroupService groupService;   // Сервис для работы с группами
    private static NoteService noteService;     // Сервис для работы с заметками
    // Экземляр сервиса для работы с пользователями
    public static LoginService getLoginService() {
        if (loginService == null) {
            loginService = new LoginService();
        }
        return loginService;
    }
    // Экземляр сервиса для работы с группами
    public static GroupService getGroupService() {
        if (groupService == null) {
            groupService = new GroupService();
        }
        return groupService;
    }
    // Экземляр сервиса для работы с заметками
    public static NoteService getNoteService() {
        if (noteService == null) {
            //noteService = new NoteService();
        }
        return noteService;
    }
}