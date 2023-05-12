package es.unizar.mii.tmdad.chatapp.service

import com.rabbitmq.http.client.Client
import com.rabbitmq.http.client.domain.BindingInfo
import com.rabbitmq.http.client.domain.ConsumerDetails
import com.rabbitmq.http.client.domain.ExchangeInfo
import org.springframework.stereotype.Service

@Service
class RabbitManageService (private val cliente: Client){

    fun getBindings(): MutableList<BindingInfo>? {
        return  cliente.bindings
    }
    fun getExchanges(): MutableList<ExchangeInfo>? {
        return cliente.exchanges
    }

    fun salas(): MutableList<ExchangeInfo>{
        val exchanges=getExchanges()
        var exchangeSalas: MutableList<ExchangeInfo> = mutableListOf()
        for (e in exchanges!!){
            if (e.arguments["tipo"]!=null){
                exchangeSalas.add(e)
            }
        }
        return exchangeSalas

    }
    fun users(): MutableList<ExchangeInfo>{
        val exchanges=getExchanges()
        var exchangeUsers: MutableList<ExchangeInfo> = mutableListOf()
        for (e in exchanges!!){
            if (e.arguments["tipo"]==null){
                exchangeUsers.add(e)
            }
        }
        return exchangeUsers

    }

    fun activeUsers(): MutableList<ConsumerDetails>? {
        return cliente.consumers
    }
}