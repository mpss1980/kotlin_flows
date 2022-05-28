package br.com.coupledev.kotlinflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.coupledev.kotlinflow.coroutines.DispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    val countDownFlow = flow<Int> {
        val startingValue = 10
        var currentValue = startingValue
        emit(startingValue)
        while (currentValue > 0) {
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }.flowOn(dispatchers.main)

    private val _stateFlow = MutableStateFlow(0)
    val stateFlow = _stateFlow.asStateFlow()

    private val _sharedFlow = MutableSharedFlow<Int>(5)
    val shareFlow = _sharedFlow.asSharedFlow()

    init {
//        orderADessertFlow()
//        collectFlowFlatMapConcat()
//        collectFlowOperators()
//        collectFlowCollect()
        squareNumber(3)
        viewModelScope.launch(dispatchers.main) {
            shareFlow.collect {
                delay(2000L)
                println("FIRST FLOW: The received number is $it")
            }
        }
        viewModelScope.launch(dispatchers.main) {
            shareFlow.collect {
                delay(3000L)
                println("SECOND FLOW: The received number is $it")
            }
        }
    }

    fun squareNumber(num: Int) {
        viewModelScope.launch(dispatchers.main) {
            _sharedFlow.emit(num * num)
        }
    }

    fun incrementCounter() {
        _stateFlow.value += 1
    }

    private fun orderADessertFlow() {
        val flow = flow {
            delay(250L)
            emit("Appetizer")
            delay(1000L)
            emit("Main Dish")
            delay(100L)
            emit("Dessert")
        }

        viewModelScope.launch {
            flow.onEach {
                println("FLOW: $it is delivered")
            }
                .buffer()
                .collect {
                    println("FLOW: Now eating $it")
                    delay(1500L)
                    println("FLOW: Finished eating $it")
                }
        }
    }

    private fun collectFlowFlatMapConcat() {
        val ids = (1..10).asFlow()

        viewModelScope.launch(dispatchers.main) {
            ids.flatMapConcat { id ->
                searchSomeDateById(id)
            }.collect { value ->
                println("The value is $value")
            }
        }
    }

    private suspend fun searchSomeDateById(id: Int): Flow<Int> {
        return flow {
            delay(1000L)
            emit(id * 5)
        }
    }

    private fun collectFlowOperators() {
        viewModelScope.launch(dispatchers.main) {
            val count = countDownFlow
                .filter { time ->
                    time % 2 == 0
                }
                .map { time ->
                    time * time
                }
                .onEach { time ->
                    println("The current time is $time")
                }
                .count {
                    it % 2 == 0
                }
            println("The count is $count")

            val reduceResult = countDownFlow
                .reduce { accumulator, value ->
                    accumulator + value
                }
            println("The reduce result is $reduceResult")

            val foldResult = countDownFlow
                .fold(100) { accumulator, value ->
                    accumulator + value
                }
            println("The fold result is $foldResult")
        }
    }

    private fun collectFlowCollect() {
        viewModelScope.launch(dispatchers.main) {
            countDownFlow.collectLatest { time ->
                delay(1500L)
                println("The current time is $time")
            }
        }
    }
}