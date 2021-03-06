package me.TechsCode.TechDiscordBot.reminders;

import me.TechsCode.TechDiscordBot.TechDiscordBot;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.*;
import java.util.stream.Collectors;

public class ReminderManager {

    private List<Reminder> reminders = new ArrayList<>();

    public void load() {
        this.reminders.addAll(TechDiscordBot.getStorage().retrieveReminders());

        checkForReminders();
    }

    private void checkForReminders() {
        new Thread(() -> {
            while (true) {
                for (Reminder reminder : new ArrayList<>(reminders)) {
                    if (Math.abs(System.currentTimeMillis() - reminder.getTime()) < 100L) {
                        reminder.send();
                        reminders.remove(reminder);
                    }
                }
            }
        }).start();
    }

    public List<Reminder> getReminders() {
        return this.reminders;
    }

    public List<Reminder> getRemindersByUser(User user) {
        return this.reminders.stream().filter(reminder -> reminder.getUserId().equals(user.getId())).collect(Collectors.toList());
    }

    public Reminder createReminder(User user, TextChannel channel, String[] args) {
        ReminderArgResponse argResponse = argsToTime(args);
        if(argResponse == null) return null;

        if(argResponse.getAmountOfArgs() == 0) {
            return null;
        } else {
            List<String> args2 = new ArrayList<>(Arrays.asList(args));
            boolean isDM = false;

            if(args2.get(args2.size() - 1).equalsIgnoreCase("dms") || args2.get(args2.size() - 1).equalsIgnoreCase("dm")) {
                args2 = args2.subList(0, args2.size() - 1);
                isDM = true;
            }

            args2 = args2.subList(argResponse.getAmountOfArgs(), args2.size());
            if(args2.size() == 0) return null;
            Reminder reminder = new Reminder(user.getId(), channel.getId(), argResponse.getTime(), argResponse.getTimeHuman(), (isDM ? ReminderType.DMs : ReminderType.CHANNEL), String.join(" ", args2));
            this.reminders.add(reminder);
            TechDiscordBot.getStorage().saveReminder(reminder);
            return reminder;
        }
    }

    public ReminderArgResponse argsToTime(String[] args) {
        HumanTimeBuilder bhb = new HumanTimeBuilder();
        long time = System.currentTimeMillis();
        int argsAm = 0;
        int i = 0;
        for(String arg : args) {
            for(ReminderTimeType rtt : ReminderTimeType.values()) {
                if(Arrays.stream(rtt.getNames()).anyMatch(n -> n.equalsIgnoreCase(arg))) {
                    if (i > 0) {
                        String tim = args[i - 1];
                        try {
                            int timint = Math.abs(Integer.parseInt(tim));
                            time = time + rtt.toMilli(timint);
                            bhb.addX(rtt, timint);
                            argsAm = argsAm + 2;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }
            }
            i++;
        }
        return new ReminderArgResponse(time, argsAm, bhb.toString());
    }

    public void deleteReminder(Reminder reminder) {
        reminder.delete();
        this.reminders.remove(reminder);
    }
}
