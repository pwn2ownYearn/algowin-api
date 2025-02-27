package org.market.hedge.binance.option.service;

import com.alibaba.fastjson.JSON;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.dto.Order;
import org.market.hedge.binance.*;
import org.market.hedge.binance.dto.BinanceException;
import org.market.hedge.binance.dto.trade.OrderSide;
import org.market.hedge.binance.dto.trade.OrderType;
import org.market.hedge.binance.dto.trade.TimeInForce;
import org.market.hedge.binance.option.BinanceOptionAuthenticated;
import org.market.hedge.binance.option.dto.trade.req.BinanceOptionOrder;
import org.market.hedge.binance.perpetualSwap.dto.trade.req.BinancePerpetualOrder;
import org.market.hedge.binance.service.BinanceTradeService;
import org.market.hedge.core.ParsingCurrencyPair;
import org.market.hedge.dto.trade.MHLimitOrder;
import org.market.hedge.dto.trade.MHMarketOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


public class BinanceOptionTradeServiceRaw extends BinanceOptionBaseService {

    protected BinanceOptionTradeServiceRaw(BinanceExchange exchange, BinanceOptionAuthenticated binance, ResilienceRegistries resilienceRegistries) {
        super(exchange, binance, resilienceRegistries);
    }

    protected String placeOrdersLimit(List<MHLimitOrder> limitOrders) throws IOException {
        OrderType type=OrderType.LIMIT;
        List<BinanceOptionOrder> batchOrders=new ArrayList<BinanceOptionOrder>();
        limitOrders.forEach(e->{
            BinanceOptionOrder newOrder =
                    BinanceOptionOrder.builder()
                            .timeInForce(byTimeInForce(e.getTimeInForce()))
                            .side(BinanceAdapters.convert(e.getType()))
                            .symbol(e.getParsingCurrencyPair().getParsing())
                            .type(type)
                            .clientOrderId(getClientOrderId(e))
                            .quantity(e.getOriginalAmount().setScale(2, RoundingMode.HALF_UP))
                            .price(e.getLimitPrice().setScale(0, RoundingMode.HALF_UP))
                            .build();
            batchOrders.add(newOrder);
        });
        BinanceOptionOrder[] strings = new BinanceOptionOrder[batchOrders.size()];
        batchOrders.toArray(strings);
        try {
            UrlParamsBuilder builder =UrlParamsBuilder.build()
                    .putToUrl("orders", JSON.toJSONString(strings))
                    .setMethod("POST");
            String signature=BinanceSignature.createSignature(apiKey,secretKey,builder);

            binance.batchOrders(JSON.toJSONString(strings),BinanceOptionApiConstants.DEFAULT_RECEIVING_WINDOW,getTimestampFactory(),apiKey,super.signatureCreator);
            return "success";
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    public TimeInForce byTimeInForce(org.market.hedge.core.TimeInForce timeInForce){
        switch (timeInForce){
            case IOC:
                return TimeInForce.IOC;
            case FOK:
                return TimeInForce.FOK;
            case PO:
            case GTC:
            default:
                return TimeInForce.GTC;

        }
    }

    protected String placeOrdersMarket(List<MHMarketOrder> marketOrder)
            throws IOException {
        OrderType type=OrderType.MARKET;
        List<BinancePerpetualOrder> batchOrders=new ArrayList<BinancePerpetualOrder>();
        marketOrder.forEach(e->{
            BinancePerpetualOrder newOrder =
                    newOrder(
                            e.getParsingCurrencyPair(),
                            BinanceAdapters.convert(e.getType()),
                            type,
                            null,
                            e.getOriginalAmount(),
                            null,
                            getClientOrderId(e),
                            null);
        });
        BinancePerpetualOrder[] strings = new BinancePerpetualOrder[batchOrders.size()];
        batchOrders.toArray(strings);

        try {
            //binance.batchOrders(JSON.toJSONString(strings),null,getTimestampFactory(),apiKey,signatureCreator);
            return "success";
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    private String getClientOrderId(Order order) {

        String clientOrderId = null;
        for (Order.IOrderFlags flags : order.getOrderFlags()) {
            if (flags instanceof BinanceTradeService.BinanceOrderFlags) {
                BinanceTradeService.BinanceOrderFlags bof = (BinanceTradeService.BinanceOrderFlags) flags;
                if (clientOrderId == null) {
                    clientOrderId = bof.getClientId();
                }
            }
        }
        return clientOrderId;
    }

    public BinancePerpetualOrder newOrder(
            ParsingCurrencyPair pair,
            OrderSide side,
            OrderType type,
            TimeInForce timeInForce,
            BigDecimal quantity,
            BigDecimal price,
            String newClientOrderId,
            BigDecimal stopPrice) {

        return new BinancePerpetualOrder(
                pair.getParsing(),
                side,
                type,
                timeInForce,
                quantity,
                price,
                newClientOrderId,
                stopPrice);

    }

    public void cancelAllOpenOrders(String symbol)
            throws IOException, BinanceException {
        binance.cancelAllOpenOrders(
                symbol,
                getRecvWindow(),
                getTimestampFactory(),
                super.apiKey,
                super.signatureCreator);
    }

}
