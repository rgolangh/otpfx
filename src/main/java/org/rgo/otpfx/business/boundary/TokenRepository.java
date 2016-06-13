package org.rgo.otpfx.business.boundary;

import org.rgo.otpfx.model.Token;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.attribute.PosixFilePermissions.*;

@Singleton
public class TokenRepository {

    private static final String OTPFX_DIR = System.getProperty("user.home") + File.separator + ".otpfx";
    private static final String OTPFX_STORE = OTPFX_DIR + File.separator + "store" + File.separator;

    @PostConstruct
    private void init() throws IOException {
        if (!Files.exists(Paths.get(OTPFX_DIR))) {
            Files.createDirectory(Paths.get(OTPFX_DIR));
        }
        if (!Files.exists(Paths.get(OTPFX_STORE))) {
            Files.createDirectory(Paths.get(OTPFX_STORE));
        }
    }

    public void save(Token token) {
        Set<PosixFilePermission> perms =
                new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        File file = null;
        try {
            Path path = Paths.get(OTPFX_STORE + token.getName());
            if (Files.notExists(path)) {
                file = Files.createFile(path, asFileAttribute(perms)).toFile();
            } else {
                file = path.toFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (
            FileOutputStream out = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(out)) {

            oos.writeObject(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<Token> get(String name) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(OTPFX_STORE + name))) {
            return Optional.of((Token) ois.readObject());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Token> getAll() {
        try {
            return Files.list(Paths.get(OTPFX_STORE))
                    .map(path -> readFromPath(path))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private Token readFromPath(Path path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (Token) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(String name) {
        try {
            Files.delete(Paths.get(OTPFX_STORE + name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
