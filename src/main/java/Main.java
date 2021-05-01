
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private static ArrayList<Long> joinedTimes = new ArrayList<>();

    private static int joinedCount = 0;

    private static long timeDif = 0;

    static int joinedTimesFlags = 0;

    static ArrayList<Long> timeStamps = new ArrayList<>();

    /////////  id's  //////////

    // channel id
    static long bots_and_more_id = 784321773449641984L;

    // mods id
    static long mods_id = 766948257170980895L;

    public static void main(String[] args) {

        String token = "Nzk1NTQxOTkxNjE0MDU0NDEz.X_K4Nw.bezhMb_P_Qqaj4goaEnk1zV8mUk";

        DiscordApi api =  new DiscordApiBuilder().setToken(token).setAllIntents().login().join();

        api.addMessageCreateListener(event -> {

            if (event.getMessageContent().equalsIgnoreCase("!timeStamps")){
                event.getChannel().sendMessage("time stamps: " + timeStamps);

                System.out.println(timeStamps);
            }

            if (event.getMessageContent().equalsIgnoreCase("!antiraid")){
                event.getChannel().sendMessage("bot");
            }
        });

        api.addServerMemberJoinListener(event -> {

            TextChannel textChannel = api.getServerTextChannelById(bots_and_more_id).get();

            long time = event.getUser().getJoinedAtTimestamp(event.getServer()).get().toEpochMilli();
            joinedTimes.add(time);
            joinedCount++;


            if (joinedTimes.size() > 1){
                textChannel.sendMessage("time (sec): " + ((joinedTimes.get(joinedTimes.size() - 1) - joinedTimes.get(joinedTimes.size() - 2)) / 1000));
                timeStamps.add((joinedTimes.get(joinedTimes.size() - 1) - joinedTimes.get(joinedTimes.size() - 2)) / 1000); // / 1000 gets values to seconds
            }

            isRaiderParty(api, event, textChannel);

        });
    }

    private static void isRaiderParty(DiscordApi api, ServerMemberJoinEvent event, TextChannel textChannel){
        // check if time between member joins is unusual
        // if so ping mods

        if (timeStamps.size() >= 2){
            for (long time : timeStamps){
                System.out.println("time " + time);
                if (time < 300L){ // 5 min time check (in seconds)
                    joinedTimesFlags++;
                    System.out.println("flag added");
                }
            }
        }

        if (joinedTimesFlags >= 3){
            textChannel.sendMessage(api.getRoleById(mods_id).get().getMentionTag()); // @Mods
            textChannel.sendMessage("group of members joined one after the other. investigate logs");
        }

        // check if account creation time is unusual
        // if so ping mods

        long accountCreationTimeMin = event.getUser().getCreationTimestamp().getEpochSecond() / 60;

        long currentTime = (joinedTimes.get(joinedTimes.size() - 1) / 1000) / 60;

        long accountLifeTime = currentTime - accountCreationTimeMin;

        textChannel.sendMessage(Long.toString(accountLifeTime));

        if (accountLifeTime < 5){
            textChannel.sendMessage(api.getRoleById(mods_id).get().getMentionTag()); // @Mods
            textChannel.sendMessage("member account was created less then 5 min ago: for " + event.getUser().getName());
            System.out.println("LOG: life time less than 5 min");
        }

    }


}
