package com.lancelot.codegentool.redis;

import com.moilioncircle.redis.replicator.Configuration;
import com.moilioncircle.redis.replicator.FileType;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.cmd.CommandParser;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueHash;
import com.moilioncircle.redis.replicator.util.ByteArrayMap;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.moilioncircle.redis.replicator.cmd.CommandParsers.toRune;

public class RedisFileDumper {
    public static void main(String[] args) throws IOException, URISyntaxException {
        // auto-generation finger print

        AtomicLong longSeq = new AtomicLong();
        Replicator replicator = new RedisReplicator(new File("E:\\tmp\\dump.rdb"), FileType.RDB,
                Configuration.defaultSetting());
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof KeyStringValueHash) {
                    KeyStringValueHash yourAppendCommand = (KeyStringValueHash) event;
                    byte[] key = yourAppendCommand.getKey();
                    try {
                        System.out.println(new String(key, "UTF-8"));
                        ByteArrayMap value = (ByteArrayMap) yourAppendCommand.getValue();
                        Set<Entry<byte[], byte[]>> entries = value.entrySet();
                        for (Entry<byte[], byte[]> entry : entries) {
                            byte[] key1 = entry.getKey();
                            byte[] value1 = entry.getValue();
                            System.out.println(new String(key1, "UTF-8") + "||||" + new String(value1, "UTF-8"));
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        replicator.open();
    }

    public static class YourAppendParser implements CommandParser<YourAppendParser.YourAppendCommand> {

        @Override
        public YourAppendCommand parse(Object[] command) {
            return new YourAppendCommand(toRune(command[1]), toRune(command[2]));
        }

        public static class YourAppendCommand implements Command {
            /**
             *
             */
            private static final long serialVersionUID = 1L;
            public final String key;
            public final String value;

            public YourAppendCommand(String key, String value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public String toString() {
                return "YourAppendCommand{" +
                        "key='" + key + '\'' +
                        ", value='" + value + '\'' +
                        '}';
            }
        }
    }
}
