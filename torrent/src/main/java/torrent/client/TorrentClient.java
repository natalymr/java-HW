package torrent.client;

import torrent.fileSystemManager.TorrentFileInfo;
import torrent.fileSystemManager.TorrentFilePart;

import java.util.List;

interface TorrentClient {

    /**
     * доступные для раздачи части определенного файла
     * @param id — идентификатор файла
     * @return - список доступных частей файла
     */
    List<Integer> stat(int id);

    /**
     * скачивание части определенного файла
     * @param id -  идентификатор файла
     * @param part - номер части
     * @return - содержимое части файла
     */
    TorrentFilePart get(int id, int part);
}
