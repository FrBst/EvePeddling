import net.troja.eve.esi.model.MarketOrdersResponse;

import java.util.Comparator;

public class PriceAsc implements Comparator<MarketOrdersResponse> {
    public int compare(MarketOrdersResponse a, MarketOrdersResponse b) {
        if (b.getPrice() > a.getPrice())
            return -1;
        if (b.getPrice() < a.getPrice())
            return 1;
        return 0;
    }
}
