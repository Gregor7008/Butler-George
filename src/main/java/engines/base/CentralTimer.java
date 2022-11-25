package engines.base;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class CentralTimer {
    
    private static CentralTimer THIS;
    
    private final Timer timer = new Timer();
    private final ConcurrentHashMap<Long, Runnable> long_runnable = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Runnable, Long> runnable_long = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TimerTask, Long> task_long = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, TimerTask> long_task = new ConcurrentHashMap<>();
    
    public static CentralTimer get() {
        if (THIS == null) {
            THIS = new CentralTimer();
        }
        return THIS;
    }
    
    
    public long schedule(Runnable runnable, TimeUnit delay_unit, long delay) {
        return this.schedule(runnable, new Date(System.currentTimeMillis() + delay_unit.toMillis(delay)));
    }
    
    public long schedule(Runnable runnable, OffsetDateTime time) {
        return this.schedule(runnable, new Date(time.toInstant().toEpochMilli()));
    }
    
    public long schedule(Runnable runnable, Date time) {
        long id = this.generateId();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    this.cancel();
                    e.printStackTrace();
                }
            }
        };
        this.long_runnable.put(id, runnable);
        this.long_task.put(id, task);
        this.runnable_long.put(task, id);
        this.task_long.put(task, id);
        timer.schedule(task, time);
        return id;
    }
    
    public long schedule(Runnable runnable, TimeUnit delay_unit, long delay, TimeUnit period_unit, long period) {
        return this.schedule(runnable, new Date(System.currentTimeMillis() + delay_unit.toMillis(delay)), period_unit, period);
    }
    
    public long schedule(Runnable runnable, OffsetDateTime time, TimeUnit period_unit, long period) {
        return this.schedule(runnable, new Date(time.toInstant().toEpochMilli()), period_unit, period);
    }
    
    public long schedule(Runnable runnable, Date firstTime, TimeUnit period_unit, long period) {
        long id = this.generateId();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    this.cancel();
                    e.printStackTrace();
                }
            }
        };
        this.long_runnable.put(id, runnable);
        this.long_task.put(id, task);
        this.runnable_long.put(task, id);
        this.task_long.put(task, id);
        timer.schedule(task, firstTime, period_unit.toMillis(period));
        return id;
    }

    public boolean reschedule(Runnable runnable, TimeUnit unit, long delay) {
        return this.reschedule(runnable, new Date(System.currentTimeMillis() + unit.toMillis(delay)));
    }
    
    public boolean reschedule(long id, TimeUnit unit, long delay) {
        return this.reschedule(id, new Date(System.currentTimeMillis() + unit.toMillis(delay)));
    }
    
    public boolean reschedule(Runnable runnable, OffsetDateTime time) {
        return this.reschedule(runnable, new Date(time.toInstant().toEpochMilli()));
    }
    
    public boolean reschedule(long id, OffsetDateTime time) {
        return this.reschedule(id, new Date(time.toInstant().toEpochMilli()));
    }
    
    public boolean reschedule(Runnable runnable, Date time) {
        long id = this.runnable_long.get(runnable);
        return this.reschedule(id, time);
    }
    
    public boolean reschedule(long id, Date time) {
        Runnable runnable = this.long_runnable.get(id);
        boolean returnValue = this.cancel(id);
        if (runnable != null) {
            this.schedule(runnable, time);
        }
        return returnValue;
    }

    public boolean reschedule(Runnable runnable, TimeUnit unit, long delay, TimeUnit period_unit, long period) {
        return this.reschedule(runnable, new Date(System.currentTimeMillis() + unit.toMillis(delay)), period_unit, period);
    }
    
    public boolean reschedule(long id, TimeUnit unit, long delay, TimeUnit period_unit, long period) {
        return this.reschedule(id, new Date(System.currentTimeMillis() + unit.toMillis(delay)), period_unit, period);
    }
    
    public boolean reschedule(Runnable runnable, OffsetDateTime time, TimeUnit period_unit, long period) {
        return this.reschedule(runnable, new Date(time.toInstant().toEpochMilli()), period_unit, period);
    }
    
    public boolean reschedule(long id, OffsetDateTime time, TimeUnit period_unit, long period) {
        return this.reschedule(id, new Date(time.toInstant().toEpochMilli()), period_unit, period);
    }
    
    public boolean reschedule(Runnable runnable, Date time, TimeUnit period_unit, long period) {
        long id = this.runnable_long.get(runnable);
        return this.reschedule(id, time, period_unit, period);
    }
    
    public boolean reschedule(long id, Date time, TimeUnit period_unit, long period) {
        Runnable runnable = this.long_runnable.get(id);
        boolean returnValue = this.cancel(id);
        if (runnable != null) {
            this.schedule(runnable, time, period_unit, period);
        }
        return returnValue;
    }
    
    public boolean cancel(Runnable runnable) {
        boolean returnValue = false;
        if (runnable != null) {
            long id = runnable_long.get(runnable);
            if (id != 0L) {
                this.long_runnable.remove(id);
                TimerTask task = long_task.get(id);
                this.long_task.remove(id);
                if (task != null) {
                    this.task_long.remove(task);
                    returnValue = task.cancel();
                }
            }
            this.runnable_long.remove(runnable);
        }
        return returnValue;
    }
    
    public boolean cancel(long id) {
        boolean returnValue = false;
        if (id != 0L) {
            Runnable runnable = long_runnable.get(id);
            this.long_runnable.remove(id);
            TimerTask task = long_task.get(id);
            this.long_task.remove(id);
            if (task != null) {
                this.task_long.remove(task);
                returnValue = task.cancel();
            }
            if (runnable != null) {
                this.runnable_long.remove(runnable);
            }
        }
        return returnValue;
    }
    
    public void cancelAll() {
        long_task.values().forEach(task -> task.cancel());
        this.long_runnable.clear();
        this.long_task.clear();
        this.runnable_long.clear();
        this.task_long.clear();
    }
    
    private long generateId() {
        long id = 0L;
        while (id == 0L || this.long_runnable.containsKey(id) || this.long_task.containsKey(id)) {
            id = ThreadLocalRandom.current().nextLong(10000000, 100000000);
        }
        return id;
    }
}