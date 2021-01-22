import processing.core.PImage;

import java.util.*;

/*
WorldModel ideally keeps track of the actual size of our grid world and what is in that world
in terms of entities and background elements
 */

final class WorldModel
{
   public int numRows;
   public int numCols;
   public Background background[][];
   public Entity occupancy[][];
   public Set<Entity> entities;

   public WorldModel(int numRows, int numCols, Background defaultBackground)
   {
      this.numRows = numRows;
      this.numCols = numCols;
      this.background = new Background[numRows][numCols];
      this.occupancy = new Entity[numRows][numCols];
      this.entities = new HashSet<>();

      for (int row = 0; row < numRows; row++)
      {
         Arrays.fill(this.background[row], defaultBackground);
      }
   }

   public boolean moveToFull(Entity octo,
                             Entity target, EventScheduler scheduler)
   {
      if (octo.position.adjacent(target.position))
      {
         return true;
      }
      else
      {
         Point nextPos = octo.nextPositionOcto(this, target.position);

         if (!octo.position.equals(nextPos))
         {
            Optional<Entity> occupant = getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            octo.moveEntity(this, nextPos);
         }
         return false;
      }
   }

   public boolean moveToNotFull(Entity octo,
                                Entity target, EventScheduler scheduler)
   {
      if (octo.position.adjacent(target.position))
      {
         octo.resourceCount += 1;
         removeEntity(target);
         scheduler.unscheduleAllEvents(target);

         return true;
      }
      else
      {
         Point nextPos = octo.nextPositionOcto(this, target.position);

         if (!octo.position.equals(nextPos))
         {
            Optional<Entity> occupant = getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            octo.moveEntity(this, nextPos);
         }
         return false;
      }
   }

   public void transformFull(Entity entity,
                             EventScheduler scheduler, ImageStore imageStore)
   {
      Entity octo = Functions.createOctoNotFull(entity.id, entity.resourceLimit,
         entity.position, entity.actionPeriod, entity.animationPeriod,
         entity.images);

      removeEntity(entity);
      scheduler.unscheduleAllEvents(entity);

      addEntity(octo);
      scheduler.scheduleActions(octo, this, imageStore);
   }

   public boolean transformNotFull(Entity entity,
                                    EventScheduler scheduler, ImageStore imageStore)
    {
       if (entity.resourceCount >= entity.resourceLimit)
       {
          Entity octo = Functions.createOctoFull(entity.id, entity.resourceLimit,
             entity.position, entity.actionPeriod, entity.animationPeriod,
             entity.images);

          removeEntity(entity);
          scheduler.unscheduleAllEvents(entity);

          addEntity(octo);
          scheduler.scheduleActions(octo, this, imageStore);

          return true;
       }

       return false;
    }

    public Optional<Point> findOpenAround(Point pos)
   {
      for (int dy = -Functions.FISH_REACH; dy <= Functions.FISH_REACH; dy++)
      {
         for (int dx = -Functions.FISH_REACH; dx <= Functions.FISH_REACH; dx++)
         {
            Point newPt = new Point(pos.x + dx, pos.y + dy);
            if (withinBounds(newPt) &&
               !isOccupied(newPt))
            {
               return Optional.of(newPt);
            }
         }
      }

      return Optional.empty();
   }

   public boolean withinBounds(Point pos)
    {
       return pos.y >= 0 && pos.y < this.numRows &&
          pos.x >= 0 && pos.x < this.numCols;
    }

    public boolean isOccupied(Point pos)
   {
      return this.withinBounds(pos) &&
         getOccupancyCell(pos) != null;
   }

   public void setOccupancyCell(Point pos,
                                Entity entity)
   {
      this.occupancy[pos.y][pos.x] = entity;
   }

   public Entity getOccupancyCell(Point pos)
   {
      return this.occupancy[pos.y][pos.x];
   }

   public Optional<Entity> findNearest(Point pos,
                                       EntityKind kind)
   {
      List<Entity> ofType = new LinkedList<>();
      for (Entity entity : this.entities)
      {
         if (entity.kind == kind)
         {
            ofType.add(entity);
         }
      }

      return Functions.nearestEntity(ofType, pos);
   }

   public Optional<Entity> getOccupant(Point pos)
   {
      if (this.isOccupied(pos))
      {
         return Optional.of(this.getOccupancyCell(pos));
      }
      else
      {
         return Optional.empty();
      }
   }

   /*
         Assumes that there is no entity currently occupying the
         intended destination cell.
      */
   public void addEntity(Entity entity)
   {
      if (this.withinBounds(entity.position))
      {
         this.setOccupancyCell(entity.position, entity);
         this.entities.add(entity);
      }
   }

   public void removeEntity(Entity entity)
   {
      removeEntityAt(entity.position);
   }

   public void removeEntityAt(Point pos)
   {
      if (this.withinBounds(pos)
         && this.getOccupancyCell(pos) != null)
      {
         Entity entity = this.getOccupancyCell(pos);

         /* this moves the entity just outside of the grid for
            debugging purposes */
         entity.position = new Point(-1, -1);
         this.entities.remove(entity);
         this.setOccupancyCell(pos, null);
      }
   }

   public Optional<PImage> getBackgroundImage(Point pos)
   {
      if (this.withinBounds(pos))
      {
         return Optional.of(Functions.getCurrentImage(getBackgroundCell(pos)));
      }
      else
      {
         return Optional.empty();
      }
   }

   public void setBackground(Point pos,
                             Background background)
   {
      if (this.withinBounds(pos))
      {
         background.setBackgroundCell(this, pos);
      }
   }

   public Background getBackgroundCell(Point pos)
    {
       return this.background[pos.y][pos.x];
    }
}
