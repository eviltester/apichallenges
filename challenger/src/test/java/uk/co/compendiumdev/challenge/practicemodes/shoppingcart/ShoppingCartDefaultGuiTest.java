package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartApiClient.RegisterResponse;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.serverstart.Environment;

class ShoppingCartDefaultGuiTest {

    private HttpMessageSender http;
    private ShoppingCartApiClient api;

    @AfterEach
    void stopApp() {
        Environment.stop();
    }

    @Test
    void defaultGuiHidesCartTokenOnCartListAndDetailPages() {
        startApp();

        final RegisterResponse cart = api.registerCart();

        final HttpResponseDetails guiCartList = http.send("/shop/gui/instances?entity=cart", "GET");
        Assertions.assertEquals(200, guiCartList.statusCode);
        Assertions.assertFalse(guiCartList.body.contains("token"));
        Assertions.assertFalse(guiCartList.body.contains(cart.token));

        final HttpResponseDetails guiCartDetail =
                http.send("/shop/gui/instance?entity=cart&id=" + cart.cartId, "GET");
        Assertions.assertEquals(200, guiCartDetail.statusCode);
        Assertions.assertFalse(guiCartDetail.body.contains("token"));
        Assertions.assertFalse(guiCartDetail.body.contains(cart.token));
    }

    @Test
    void defaultGuiDoesNotExposeInternalCartItemEntityMenu() {
        startApp();

        final HttpResponseDetails guiEntities = http.send("/shop/gui/entities", "GET");
        Assertions.assertEquals(200, guiEntities.statusCode);
        Assertions.assertTrue(guiEntities.body.contains("entity=cart"));
        Assertions.assertTrue(guiEntities.body.contains("entity=product"));
        Assertions.assertFalse(guiEntities.body.contains("entity=cartitem"));
    }

    private void startApp(final String... extraArgs) {
        Environment.stop();
        final List<String> args = new ArrayList<>();
        args.add("-multiplayer");
        args.add("-nostorage");
        args.addAll(List.of(extraArgs));
        ChallengeMain.main(args.toArray(String[]::new));
        Environment.waitTillRunningStatus(true);
        http = new HttpMessageSender("http://localhost:4567");
        api = new ShoppingCartApiClient(http);
    }
}
