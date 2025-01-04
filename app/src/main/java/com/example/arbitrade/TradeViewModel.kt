import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.arbitrade.TradeData

class TradeViewModel : ViewModel() {
    // MutableLiveData to hold the tradeList
    val tradeList = MutableLiveData<MutableList<TradeData>>().apply { value = mutableListOf() }

    // Function to add a new trade to the list
    fun addTrade(trade: TradeData) {
        val list = tradeList.value ?: mutableListOf()
        list.add(trade)
        tradeList.value = list
    }

    // Function to update an existing trade in the list
    fun updateTrade(position: Int, updatedTrade: TradeData) {
        val list = tradeList.value ?: mutableListOf()
        if (position != -1) {
            list[position] = updatedTrade
            tradeList.value = list
        }
    }

    // Function to delete a trade from the list
    fun deleteTrade(trade: TradeData) {
        val list = tradeList.value ?: mutableListOf()
        list.remove(trade)
        tradeList.value = list
    }
}