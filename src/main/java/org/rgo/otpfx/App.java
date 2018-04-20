package org.rgo.otpfx;

import com.airhacks.afterburner.injection.InjectionProvider;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.rgo.otpfx.business.boundary.TokenRepository;
import org.rgo.otpfx.model.Token;
import org.rgo.otpfx.presentation.token.TokenView;

public class App extends Application {

    public static void main(String[] args) {
        if (args.length > 0) {
            cli(args[0]);
        } else {
            launch(args);
        }
    }

    private static void cli(String arg) {
        switch (arg) {
        case "-f":
            TokenRepository tokenRepository = new TokenRepository();
            Token token = tokenRepository.getAll().get(0);
            String now = token.now();
            tokenRepository.save(token);
            System.out.println(now);
            break;
        }
        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(getClass().getResource("awesome.ttf").toExternalForm(), 12);
        Scene scene = new Scene(new TokenView().getView());
        scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
        stage.setTitle("otpfx v1.0-SNAPSHOT");
        stage.setScene(scene);
        stage.getIcons().add(new Image("file://" + getClass().getResource("key.svg").getPath()));
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        InjectionProvider.forgetAll();
    }
}
