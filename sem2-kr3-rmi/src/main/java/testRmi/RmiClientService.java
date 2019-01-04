package testRmi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface RmiClientService {

    // на удаленном экземпляре класса clazz вызывается метод method с аргументами params
    Object invokeStatic(Class<?> clazz, Method method, Object... params);

    // инициализирует удаленного двойника для localReceiverReference
    // с помощью конструктора constructor и аргументов params
    void init(Object localReceiverReference, Constructor constructor, Object... params);

    // якобы на localReceiverReference,
    // но на самом деле на удаленном его двойнике
    // вызывается метод method с аргументами params
    Object invokeOn(Object localReceiverReference, Method method, Object... params);
}