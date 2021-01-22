import java.util.List;
import java.util.Optional;

import processing.core.PImage;

/*
Entity ideally would includes functions for how all the entities in our virtual world might act...
 */


final class Entity
{
   public EntityKind kind;
   public String id;
   public Point position;
   public List<PImage> images;
   public int imageIndex;
   public int resourceLimit;
   public int resourceCount;
   public int actionPeriod;
   public int animationPeriod;

   public Entity(EntityKind kind, String id, Point position,
      List<PImage> images, int resourceLimit, int resourceCount,
      int actionPeriod, int animationPeriod)
   {
      this.kind = kind;
      this.id = id;
      this.position = position;
      this.images = images;
      this.imageIndex = 0;
      this.resourceLimit = resourceLimit;
      this.resourceCount = resourceCount;
      this.actionPeriod = actionPeriod;
      this.animationPeriod = animationPeriod;
   }

   public Point nextPositionOcto(WorldModel world,
                                 Point destPos)
   {
      int horiz = Integer.signum(destPos.x - this.position.x);
      Point newPos = new Point(this.position.x + horiz,
         this.position.y);

      if (horiz == 0 || world.isOccupied(newPos))
      {
         int vert = Integer.signum(destPos.y - this.position.y);
         newPos = new Point(this.position.x,
            this.position.y + vert);

         if (vert == 0 || world.isOccupied(newPos))
         {
            newPos = this.position;
         }
      }

      return newPos;
   }

   public Point nextPositionCrab(WorldModel world,
                                 Point destPos)
   {
      int horiz = Integer.signum(destPos.x - this.position.x);
      Point newPos = new Point(this.position.x + horiz,
         this.position.y);

      Optional<Entity> occupant = world.getOccupant(newPos);

      if (horiz == 0 ||
         (occupant.isPresent() && !(occupant.get().kind == EntityKind.FISH)))
      {
         int vert = Integer.signum(destPos.y - this.position.y);
         newPos = new Point(this.position.x, this.position.y + vert);
         occupant = world.getOccupant(newPos);

         if (vert == 0 ||
            (occupant.isPresent() && !(occupant.get().kind == EntityKind.FISH)))
         {
            newPos = this.position;
         }
      }

      return newPos;
   }

   public Action createActivityAction(WorldModel world,
                                      ImageStore imageStore)
   {
      return new Action(ActionKind.ACTIVITY, this, world, imageStore, 0);
   }

   public Action createAnimationAction(int repeatCount)
   {
      return new Action(ActionKind.ANIMATION, this, null, null, repeatCount);
   }

   public void tryAddEntity(WorldModel world)
   {
      if (world.isOccupied(this.position))
      {
         // arguably the wrong type of exception, but we are not
         // defining our own exceptions yet
         throw new IllegalArgumentException("position occupied");
      }

      world.addEntity(this);
   }

   public boolean moveToCrab(WorldModel world,
                              Entity target, EventScheduler scheduler)
    {
       if (this.position.adjacent(target.position))
       {
          world.removeEntity(target);
          scheduler.unscheduleAllEvents(target);
          return true;
       }
       else
       {
          Point nextPos = this.nextPositionCrab(world, target.position);

          if (!this.position.equals(nextPos))
          {
             Optional<Entity> occupant = world.getOccupant(nextPos);
             if (occupant.isPresent())
             {
                scheduler.unscheduleAllEvents(occupant.get());
             }

             moveEntity(world, nextPos);
          }
          return false;
       }
    }

    public void moveEntity(WorldModel world, Point pos)
   {
      Point oldPos = this.position;
      if (world.withinBounds(pos) && !pos.equals(oldPos))
      {
         world.setOccupancyCell(oldPos, null);
         world.removeEntityAt(pos);
         world.setOccupancyCell(pos, this);
         this.position = pos;
      }
   }

   public void nextImage()
   {
      this.imageIndex = (this.imageIndex + 1) % this.images.size();
   }

   public int getAnimationPeriod()
    {
       switch (this.kind)
       {
          case OCTO_FULL:
          case OCTO_NOT_FULL:
          case CRAB:
          case QUAKE:
          case ATLANTIS:
             return this.animationPeriod;
       default:
          throw new UnsupportedOperationException(
             String.format("getAnimationPeriod not supported for %s",
             this.kind));
       }
    }
}
