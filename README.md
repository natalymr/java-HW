# Server Performance Tester

## Запуск приложения

Для запуска приложения необходимо:
1. **собрать проект**, для этого в корневой директории проекта выполнить команду 
  `./gradlew installDist`
2. **запустить сервер**, для этого нужно выполнить команду 
  `./server/build/install/server/bin/server`
3. **запустить GUI**, для этого нужно выполнить команду 
  `./build/install/performance_tester/bin/performance_tester`

## GUI

### Start Page

![start](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/start.png?raw=true)

## Results

### Server 1: thread per client

![result1](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/resultsThreadPerClient.png?raw=true)

### Server 2: sort in thread pool

![result2](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/resultsSortInThreadPool.png?raw=true)

### Server 3: non blocking 

![result3](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/resultsNonBlocking.png?raw=true)

## Application Architecture

### Sequence Diagram

![uml](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/uml.png?raw=true)
