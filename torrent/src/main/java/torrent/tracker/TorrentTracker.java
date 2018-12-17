package torrent.tracker;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import torrent.fileSystemManager.TorrentFileInfo;
import torrent.client.TorrentClientInfo;

interface TorrentTracker {

    /**
     * один из возможных запросов к Torrent-tracker
     * @return список раздаваемых файлов
     */
    Set<TorrentFileInfo> list();

    /**
     * публикация нового файла
     * @param newFileName - название файла
     * @param newFileSize - размер файла
     * @return - id — идентификатор файла
     */
    int upload(String newFileName, long newFileSize) throws IOException;

    /**
     * возвращает список клиентов, владеющих определенным файлов целиком или некоторыми его частями
     * @param id - идентификатор файла
     * @return - список из TorrentClientInfo, каждый из которых хранит IP и port клиента
     */
    List<TorrentClientInfo> sources(int id);

    /**
     * загрузка клиентом данных о раздаваемых файлах
     * @param clientInfo - клиент
     * @param fileIDs - раздаваемые файлы
     * @return - True, если информация успешно обновлена
     */
    boolean update(TorrentClientInfo clientInfo, List<Integer> fileIDs);
}
