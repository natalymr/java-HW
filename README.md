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
#### M

![r_thread_M](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_thread_M.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_threadPerClient_M_1548209114937.csv) с данными

#### N

![r_thread_N](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_thread_N.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_threadPerClient_N_1548209468469.csv) с данными

#### delay

![r_thread_delay](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_thread_delay.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_threadPerClient_delay_1548210257360.csv) с данными

### Server 2: sort in thread pool

#### M

![r_pool_M](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_pool_M.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_sortInThreadPool_M_1548210556228.csv) с данными

#### N

![r_pool_N](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_pool_N.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_sortInThreadPool_N_1548210912712.csv) с данными

#### delay

![r_pool_delay](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_pool_delay.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_sortInThreadPool_delay_1548211720067.csv) с данными

### Server 3: non blocking 

#### M

![r_nonblock_M](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_nonBlocking_M.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_nonBlockingServer_M_1548212039685.csv) с данными

#### N

![r_nonblock_N](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_nonBlocking_N.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_nonBlockingServer_N_1548212455038.csv) с данными

#### delay

![r_nonblock_delay](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/results_nonBlocking_delay.png?raw=true)

> [Файл](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/performance_tester/PerformanceTesterResults_nonBlockingServer_delay_1548213217262.csv) с данными

## Application Architecture

### Sequence Diagram

![uml](https://github.com/natalymr/spbau_java_hw/blob/sem2-AM/pictures/uml.png?raw=true)
