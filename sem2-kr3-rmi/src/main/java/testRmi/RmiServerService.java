package testRmi;

public interface RmiServerService {

    // запускает обработку подключений на указанном порту
    // каждую команду на исполнение запускает в отдельном потоке (см пул потоков)
    void launch(short port);
}