import net.troja.eve.esi.model.MarketOrdersResponse;

import java.util.Comparator;

public class TypeAsc implements Comparator<MarketOrdersResponse> {
    public int compare(MarketOrdersResponse a, MarketOrdersResponse b) {
        if (b.getTypeId() > a.getTypeId())
            return -1;
        if (b.getTypeId() < a.getTypeId())
            return 1;
        return 0;
    }
}
