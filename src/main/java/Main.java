import net.troja.eve.esi.*;
import net.troja.eve.esi.api.MarketApi;
import net.troja.eve.esi.api.UniverseApi;
import net.troja.eve.esi.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static List<Integer> resolveSystemNames(List<String> names) throws ApiException {
        UniverseApi universe = new UniverseApi();
        List<UniverseIdsSystem> response =
                universe.postUniverseIds(names, null, "tranquility", null).getSystems();

        return  response != null ? response.stream().map(UniverseIdsSystem::getId).collect(Collectors.toList()) :
                Collections.emptyList();
    }

    private static Set<Integer> getRegions(List<Integer> systems) throws ApiException {
        UniverseApi universe = new UniverseApi();
        SystemResponse systemResponse;
        ConstellationResponse constellationResponse;
        Set<Integer> regions = new HashSet<>();

        for (Integer systemID : systems) {
            systemResponse = universe.getUniverseSystemsSystemId(systemID,
                    null, "tranquility", null, null);
            constellationResponse = universe.getUniverseConstellationsConstellationId(systemResponse.getConstellationId(),
                    null, "tranquility", null, null);
            regions.add(constellationResponse.getRegionId());
        }

        return regions;
    }

    private static List<MarketOrdersResponse> getOrders (List<Integer> systems, String order_type) throws ApiException {
        Set<Integer> regions = getRegions(systems);
        MarketApi market = new MarketApi();
        List<MarketOrdersResponse> marketOrdersResponses;
        int page = 1;
        List<MarketOrdersResponse> appropriateOrders = new ArrayList<>();
        for (int region : regions) {
            try {
                do {
                    marketOrdersResponses = market.getMarketsRegionIdOrders(order_type,
                            region, "tranquility", null, page, null);
                    for (MarketOrdersResponse response : marketOrdersResponses) {
                        for (int system : systems) {
                            if (response.getSystemId() == system)
                                appropriateOrders.add(response);
                        }
                    }
                    page++;
                } while (marketOrdersResponses.size() > 0);
            } catch (ApiException e) { }
            page = 1;
        }

        if (order_type.equals("buy"))
            appropriateOrders.sort(new PriceAsc());
        else
            appropriateOrders.sort(new PriceDesc());
        appropriateOrders.sort(new TypeAsc());
        return appropriateOrders;
    }

    private static List<Deal> solve(List<Integer> startSystems, List<Integer> endSystems, int capital, double storage) throws ApiException {
        List<MarketOrdersResponse> toBuy;
        List<MarketOrdersResponse> toSell;
        List<Deal> deals;
//        while (true) {
//            try {
                toBuy = getOrders(startSystems, "sell");
                toSell = getOrders(endSystems, "buy");
                deals = new ArrayList<>();
//                break;
//            } catch (ApiException e) {
//                System.out.println("API exception: " + e.getMessage());
//            }
//        }
        int bo = 0;
        int so = 0;
        int buyAvailable = toBuy.get(bo).getVolumeRemain();
        int sellAvailable = toSell.get(so).getVolumeRemain();
        int batch;
        while (bo < toBuy.size() && so < toSell.size()) {
            if (toSell.get(so).getTypeId() < toBuy.get(bo).getTypeId()) {
                so++;
                if (so < toSell.size())
                    sellAvailable = toSell.get(so).getVolumeRemain();
                continue;
            }
            if (toSell.get(so).getTypeId() > toBuy.get(bo).getTypeId()) {
                bo++;
                if (bo < toBuy.size())
                    buyAvailable = toBuy.get(bo).getVolumeRemain();
                continue;
            }
            if (toBuy.get(bo).getPrice() > toSell.get(so).getPrice() || buyAvailable == 0) {
                bo++;
                if (bo < toBuy.size())
                    buyAvailable = toBuy.get(bo).getVolumeRemain();
                continue;
            }
            if (sellAvailable == 0) {
                so++;
                if (so < toSell.size())
                    sellAvailable = toSell.get(so).getVolumeRemain();
                continue;
            }

            batch = Math.min(sellAvailable, buyAvailable);
            sellAvailable -= batch;
            buyAvailable -= batch;
            deals.add(new Deal(toBuy.get(bo).getTypeId(),
                    batch,
                    toBuy.get(bo).getPrice(),
                    toSell.get(so).getPrice(),
                    toBuy.get(bo).getSystemId(),
                    toSell.get(so).getSystemId()));
        }

        deals.sort(new MarginDesc());
        return deals;
    }

    public static List<String> interpret (List<Deal> deals) throws ApiException {
        List<String> ans = new LinkedList<>();

        Set<Integer> temp = new HashSet<>();
        for (Deal deal : deals) {
            temp.add(deal.getBuySystem());
            temp.add(deal.getSellSystem());
            temp.add(deal.getType());
        }

        List<Integer> ids = new ArrayList<>(temp);
        UniverseApi universe = new UniverseApi();
        List<UniverseNamesResponse> response;
        response = universe.postUniverseNames( ids, "tranquility");

        Map<Integer, String> names = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            names.put(response.get(i).getId(), response.get(i).getName());
        }

        for (Deal deal : deals) {
            String line = "";
            line += names.get(deal.getBuySystem()) + " buy " + deal.getCount() + "x " + names.get(deal.getType())
                    + " for " + deal.getBuyPrice() + " ISK, " + names.get(deal.getSellSystem()) + " sell for "
                    + deal.getSellPrice() + " ISK; Margin " + deal.getMargin();
            ans.add(line);
        }

        return ans;
    }

    public static void main(String[] args) throws ApiException {
        System.out.println("Executing...");
//        UniverseApi universe = new UniverseApi();
//        List<UniverseNamesResponse> response;
//        List<Integer> ids = new ArrayList<Integer>();
//        ids.add(12614);
//        response = universe.postUniverseNames( ids, "tranquility");

        List<Deal> deals = solve(resolveSystemNames(Arrays.asList("Amarr", "Jita")), resolveSystemNames(Arrays.asList("Pasha", "Tash-Murkon Prime")), 0, 0);
        List<String> results = interpret(deals);
        for (String s : results) {
            System.out.println(s);
        }
    }
}
