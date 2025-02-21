package xyz.nkomarn.harbor.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import xyz.nkomarn.harbor.Harbor;
import xyz.nkomarn.harbor.util.Config;

import java.util.function.Consumer;

public class AccelerateNightTask implements Consumer<ScheduledTask> {

    private final Harbor harbor;
    private final Checker checker;
    private final World world;

    public AccelerateNightTask(@NotNull Harbor harbor, @NotNull Checker checker, @NotNull World world) {
        this.harbor = harbor;
        this.checker = checker;
        this.world = world;

        harbor.getMessages().sendRandomChatMessage(world, "messages.chat.night-skipping");
        checker.clearWeather(world);
    }

    @Override
    public void accept(ScheduledTask task) {
        Config config = harbor.getConfiguration();

        long time = world.getTime();
        double timeRate = config.getInteger("night-skip.time-rate");
        int dayTime = Math.max(150, config.getInteger("night-skip.daytime-ticks"));
        int sleeping = checker.getSleepingPlayers(world).size();

        if (config.getBoolean("night-skip.proportional-acceleration")) {
            timeRate = Math.min(timeRate, Math.round(timeRate / world.getPlayers().size() * Math.max(1, sleeping)));
        }

        if (time >= (dayTime - timeRate * 1.5) && time <= dayTime) {
            if (config.getBoolean("night-skip.reset-phantom-statistic")) {
                world.getPlayers().forEach(player -> player.setStatistic(Statistic.TIME_SINCE_REST, 0));
            }

            checker.resetStatus(world);
            task.cancel();
            return;
        }

        world.setTime(time + (int) timeRate);
    }
}
