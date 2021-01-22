/*
Action: ideally what our various entities might do in our virutal world
 */

final class Action
{
   public ActionKind kind;
   public Entity entity;
   public WorldModel world;
   public ImageStore imageStore;
   public int repeatCount;

   public Action(ActionKind kind, Entity entity, WorldModel world,
      ImageStore imageStore, int repeatCount)
   {
      this.kind = kind;
      this.entity = entity;
      this.world = world;
      this.imageStore = imageStore;
      this.repeatCount = repeatCount;
   }

   public void executeActivityAction(EventScheduler scheduler)
   {
      switch (this.entity.kind)
      {
      case OCTO_FULL:
         scheduler.executeOctoFullActivity(this.entity, this.world,
            this.imageStore);
         break;

      case OCTO_NOT_FULL:
         scheduler.executeOctoNotFullActivity(this.entity, this.world,
            this.imageStore);
         break;

      case FISH:
         scheduler.executeFishActivity(this.entity, this.world, this.imageStore
         );
         break;

      case CRAB:
         scheduler.executeCrabActivity(this.entity, this.world,
            this.imageStore);
         break;

      case QUAKE:
         scheduler.executeQuakeActivity(this.entity, this.world, this.imageStore
         );
         break;

      case SGRASS:
         scheduler.executeSgrassActivity(this.entity, this.world, this.imageStore
         );
         break;

      case ATLANTIS:
         scheduler.executeAtlantisActivity(this.entity, this.world, this.imageStore
         );
         break;

      default:
         throw new UnsupportedOperationException(
            String.format("executeActivityAction not supported for %s",
            this.entity.kind));
      }
   }

   public void executeAnimationAction(EventScheduler scheduler)
   {
      this.entity.nextImage();

      if (this.repeatCount != 1)
      {
         scheduler.scheduleEvent(this.entity,
            this.entity.createAnimationAction(
                    Math.max(this.repeatCount - 1, 0)),
            this.entity.getAnimationPeriod());
      }
   }

   public void executeAction(EventScheduler scheduler)
    {
       switch (this.kind)
       {
       case ACTIVITY:
          this.executeActivityAction(scheduler);
          break;

       case ANIMATION:
          this.executeAnimationAction(scheduler);
          break;
       }
    }
}
