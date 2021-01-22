import java.util.*;

/*
EventScheduler: ideally our way of controlling what happens in our virtual world
 */

final class EventScheduler
{
   public PriorityQueue<Event> eventQueue;
   public Map<Entity, List<Event>> pendingEvents;
   public double timeScale;

   public EventScheduler(double timeScale)
   {
      this.eventQueue = new PriorityQueue<>(new EventComparator());
      this.pendingEvents = new HashMap<>();
      this.timeScale = timeScale;
   }

   public void removePendingEvent(Event event)
   {
      List<Event> pending = this.pendingEvents.get(event.entity);

      if (pending != null)
      {
         pending.remove(event);
      }
   }

   public void unscheduleAllEvents(Entity entity)
   {
      List<Event> pending = this.pendingEvents.remove(entity);

      if (pending != null)
      {
         for (Event event : pending)
         {
            this.eventQueue.remove(event);
         }
      }
   }

   public void scheduleEvent(Entity entity, Action action, long afterPeriod)
    {
       long time = System.currentTimeMillis() +
          (long)(afterPeriod * this.timeScale);
       Event event = new Event(action, time, entity);

       this.eventQueue.add(event);

       // update list of pending events for the given entity
       List<Event> pending = this.pendingEvents.getOrDefault(entity,
          new LinkedList<>());
       pending.add(event);
       this.pendingEvents.put(entity, pending);
    }

    public void scheduleActions(Entity entity,
                               WorldModel world, ImageStore imageStore)
   {
      switch (entity.kind)
      {
      case OCTO_FULL:
         this.scheduleEvent(entity,
            entity.createActivityAction(world, imageStore),
            entity.actionPeriod);
         this.scheduleEvent(entity, entity.createAnimationAction(0),
            entity.getAnimationPeriod());
         break;

      case OCTO_NOT_FULL:
         this.scheduleEvent(entity,
            entity.createActivityAction(world, imageStore),
            entity.actionPeriod);
         this.scheduleEvent(entity,
            entity.createAnimationAction(0), entity.getAnimationPeriod());
         break;

      case FISH:
         this.scheduleEvent(entity,
            entity.createActivityAction(world, imageStore),
            entity.actionPeriod);
         break;

      case CRAB:
         this.scheduleEvent(entity,
            entity.createActivityAction(world, imageStore),
            entity.actionPeriod);
         this.scheduleEvent(entity,
            entity.createAnimationAction(0), entity.getAnimationPeriod());
         break;

      case QUAKE:
         this.scheduleEvent(entity,
            entity.createActivityAction(world, imageStore),
            entity.actionPeriod);
         this.scheduleEvent(entity,
            entity.createAnimationAction(Functions.QUAKE_ANIMATION_REPEAT_COUNT),
            entity.getAnimationPeriod());
         break;

      case SGRASS:
         this.scheduleEvent(entity,
            entity.createActivityAction(world, imageStore),
            entity.actionPeriod);
         break;
      case ATLANTIS:
         this.scheduleEvent(entity,
                    entity.createAnimationAction(Functions.ATLANTIS_ANIMATION_REPEAT_COUNT),
                    entity.getAnimationPeriod());
            break;

      default:
      }
   }

   public void executeSgrassActivity(Entity entity, WorldModel world,
                                     ImageStore imageStore)
   {
      Optional<Point> openPt = world.findOpenAround(entity.position);

      if (openPt.isPresent())
      {
         Entity fish = Functions.createFish(Functions.FISH_ID_PREFIX + entity.id,
                 openPt.get(), Functions.FISH_CORRUPT_MIN +
                         Functions.rand.nextInt(Functions.FISH_CORRUPT_MAX - Functions.FISH_CORRUPT_MIN),
                 imageStore.getImageList(Functions.FISH_KEY));
         world.addEntity(fish);
         this.scheduleActions(fish, world, imageStore);
      }

      this.scheduleEvent(entity,
         entity.createActivityAction(world, imageStore),
         entity.actionPeriod);
   }

   public void executeAtlantisActivity(Entity entity, WorldModel world,
                                       ImageStore imageStore)
   {
      this.unscheduleAllEvents(entity);
      world.removeEntity(entity);
   }

   public void executeQuakeActivity(Entity entity, WorldModel world,
                                    ImageStore imageStore)
   {
      this.unscheduleAllEvents(entity);
      world.removeEntity(entity);
   }

   public void executeCrabActivity(Entity entity, WorldModel world,
                                   ImageStore imageStore)
   {
      Optional<Entity> crabTarget = world.findNearest(
              entity.position, EntityKind.SGRASS);
      long nextPeriod = entity.actionPeriod;

      if (crabTarget.isPresent())
      {
         Point tgtPos = crabTarget.get().position;

         if (entity.moveToCrab(world, crabTarget.get(), this))
         {
            Entity quake = Functions.createQuake(tgtPos,
               imageStore.getImageList(Functions.QUAKE_KEY));

            world.addEntity(quake);
            nextPeriod += entity.actionPeriod;
            this.scheduleActions(quake, world, imageStore);
         }
      }

      this.scheduleEvent(entity,
         entity.createActivityAction(world, imageStore),
         nextPeriod);
   }

   public void executeFishActivity(Entity entity, WorldModel world,
                                   ImageStore imageStore)
   {
      Point pos = entity.position;  // store current position before removing

      world.removeEntity(entity);
      this.unscheduleAllEvents(entity);

      Entity crab = Functions.createCrab(entity.id + Functions.CRAB_ID_SUFFIX,
              pos, entity.actionPeriod / Functions.CRAB_PERIOD_SCALE,
              Functions.CRAB_ANIMATION_MIN +
                      Functions.rand.nextInt(Functions.CRAB_ANIMATION_MAX - Functions.CRAB_ANIMATION_MIN),
              imageStore.getImageList(Functions.CRAB_KEY));

      world.addEntity(crab);
      this.scheduleActions(crab, world, imageStore);
   }

   public void executeOctoNotFullActivity(Entity entity,
                                          WorldModel world, ImageStore imageStore)
   {
      Optional<Entity> notFullTarget = world.findNearest(entity.position,
         EntityKind.FISH);

      if (!notFullTarget.isPresent() ||
         !world.moveToNotFull(entity, notFullTarget.get(), this) ||
         !world.transformNotFull(entity, this, imageStore))
      {
         this.scheduleEvent(entity,
            entity.createActivityAction(world, imageStore),
            entity.actionPeriod);
      }
   }

   public void executeOctoFullActivity(Entity entity, WorldModel world,
                                       ImageStore imageStore)
   {
      Optional<Entity> fullTarget = world.findNearest(entity.position,
         EntityKind.ATLANTIS);

      if (fullTarget.isPresent() &&
         world.moveToFull(entity, fullTarget.get(), this))
      {
         //at atlantis trigger animation
         this.scheduleActions(fullTarget.get(), world, imageStore);

         //transform to unfull
         world.transformFull(entity, this, imageStore);
      }
      else
      {
         this.scheduleEvent(entity,
            entity.createActivityAction(world, imageStore),
            entity.actionPeriod);
      }
   }

   public void updateOnTime(long time)
    {
       while (!this.eventQueue.isEmpty() &&
          this.eventQueue.peek().time < time)
       {
          Event next = this.eventQueue.poll();

          this.removePendingEvent(next);

          next.action.executeAction(this);
       }
    }
}
