package zad1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public final class Futil {
    static void processDir(String directory, String resultFileName) {
        Path dir = Paths.get(directory);
        Path result = Paths.get(resultFileName);

        try (FileChannel resultChannel = FileChannel.open(result, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes fileAttributes) throws IOException {
                    if (Files.isReadable(file) && Files.isRegularFile(file)) {
                        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                            CharsetDecoder decoder = Charset.forName("Cp1250").newDecoder();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            while (channel.read(buffer) != -1) {
                                buffer.flip();
                                String s = decoder.decode(buffer).toString();
                                buffer.clear();
                                resultChannel.write(ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)));
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;}
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
