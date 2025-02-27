package org.market.hedge.coinex;

import org.market.hedge.coinex.dto.account.CoinexBalances;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CoinexAuthenticated {

  String HEADER_AUTHORIZATION = "authorization";
  String HEADER_USER_AGENT = "User-Agent";

  @GET
  @Path("balance/info")
  CoinexBalances getWallet(
      //            @HeaderParam(HEADER_USER_AGENT) String user_agent_info,
      @HeaderParam(HEADER_AUTHORIZATION) ParamsDigest sign,
      @QueryParam("access_id") String access_id,
      @QueryParam("tonce") SynchronizedValueFactory<Long> tonce)
      throws IOException;
}
