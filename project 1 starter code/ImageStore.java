import java.util.*;

import processing.core.PApplet;
import processing.core.PImage;

/*
ImageStore: to ideally keep track of the images used in our virtual world
 */

final class ImageStore
{
   public Map<String, List<PImage>> images;
   public List<PImage> defaultImages;

   public ImageStore(PImage defaultImage)
   {
      this.images = new HashMap<>();
      defaultImages = new LinkedList<>();
      defaultImages.add(defaultImage);
   }

   public void load(Scanner in, WorldModel world)
   {
      int lineNumber = 0;
      while (in.hasNextLine())
      {
         try
         {
            if (!processLine(in.nextLine(), world))
            {
               System.err.println(String.format("invalid entry on line %d",
                  lineNumber));
            }
         }
         catch (NumberFormatException e)
         {
            System.err.println(String.format("invalid entry on line %d",
               lineNumber));
         }
         catch (IllegalArgumentException e)
         {
            System.err.println(String.format("issue on line %d: %s",
               lineNumber, e.getMessage()));
         }
         lineNumber++;
      }
   }

   public boolean parseSgrass(String[] properties, WorldModel world)
   {
      if (properties.length == Functions.SGRASS_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[Functions.SGRASS_COL]),
            Integer.parseInt(properties[Functions.SGRASS_ROW]));
         Entity entity = Functions.createSgrass(properties[Functions.SGRASS_ID],
            pt,
            Integer.parseInt(properties[Functions.SGRASS_ACTION_PERIOD]),
            getImageList(Functions.SGRASS_KEY));
         entity.tryAddEntity(world);
      }

      return properties.length == Functions.SGRASS_NUM_PROPERTIES;
   }

   public boolean parseAtlantis(String[] properties, WorldModel world)
   {
      if (properties.length == Functions.ATLANTIS_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[Functions.ATLANTIS_COL]),
            Integer.parseInt(properties[Functions.ATLANTIS_ROW]));
         Entity entity = Functions.createAtlantis(properties[Functions.ATLANTIS_ID],
            pt, getImageList(Functions.ATLANTIS_KEY));
         entity.tryAddEntity(world);
      }

      return properties.length == Functions.ATLANTIS_NUM_PROPERTIES;
   }

   public boolean parseFish(String[] properties, WorldModel world)
   {
      if (properties.length == Functions.FISH_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[Functions.FISH_COL]),
            Integer.parseInt(properties[Functions.FISH_ROW]));
         Entity entity = Functions.createFish(properties[Functions.FISH_ID],
            pt, Integer.parseInt(properties[Functions.FISH_ACTION_PERIOD]),
            getImageList(Functions.FISH_KEY));
         entity.tryAddEntity(world);
      }

      return properties.length == Functions.FISH_NUM_PROPERTIES;
   }

   public boolean parseObstacle(String[] properties, WorldModel world)
   {
      if (properties.length == Functions.OBSTACLE_NUM_PROPERTIES)
      {
         Point pt = new Point(
            Integer.parseInt(properties[Functions.OBSTACLE_COL]),
            Integer.parseInt(properties[Functions.OBSTACLE_ROW]));
         Entity entity = Functions.createObstacle(properties[Functions.OBSTACLE_ID],
            pt, getImageList(Functions.OBSTACLE_KEY));
         entity.tryAddEntity(world);
      }

      return properties.length == Functions.OBSTACLE_NUM_PROPERTIES;
   }

   public boolean parseOcto(String[] properties, WorldModel world)
   {
      if (properties.length == Functions.OCTO_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[Functions.OCTO_COL]),
            Integer.parseInt(properties[Functions.OCTO_ROW]));
         Entity entity = Functions.createOctoNotFull(properties[Functions.OCTO_ID],
            Integer.parseInt(properties[Functions.OCTO_LIMIT]),
            pt,
            Integer.parseInt(properties[Functions.OCTO_ACTION_PERIOD]),
            Integer.parseInt(properties[Functions.OCTO_ANIMATION_PERIOD]),
            getImageList(Functions.OCTO_KEY));
         entity.tryAddEntity(world);
      }

      return properties.length == Functions.OCTO_NUM_PROPERTIES;
   }

   public boolean parseBackground(String[] properties,
                                  WorldModel world)
   {
      if (properties.length == Functions.BGND_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[Functions.BGND_COL]),
            Integer.parseInt(properties[Functions.BGND_ROW]));
         String id = properties[Functions.BGND_ID];
         world.setBackground(pt,
            new Background(id, getImageList(id)));
      }

      return properties.length == Functions.BGND_NUM_PROPERTIES;
   }

   public boolean processLine(String line, WorldModel world)
    {
       String[] properties = line.split("\\s");
       if (properties.length > 0)
       {
          switch (properties[Functions.PROPERTY_KEY])
          {
          case Functions.BGND_KEY:
             return this.parseBackground(properties, world);
          case Functions.OCTO_KEY:
             return this.parseOcto(properties, world);
          case Functions.OBSTACLE_KEY:
             return this.parseObstacle(properties, world);
          case Functions.FISH_KEY:
             return this.parseFish(properties, world);
          case Functions.ATLANTIS_KEY:
             return this.parseAtlantis(properties, world);
          case Functions.SGRASS_KEY:
             return this.parseSgrass(properties, world);
          }
       }

       return false;
    }

    public void loadImages(Scanner in,
                           PApplet screen)
    {
       int lineNumber = 0;
       while (in.hasNextLine())
       {
          try
          {
             Functions.processImageLine(this.images, in.nextLine(), screen);
          }
          catch (NumberFormatException e)
          {
             System.out.println(String.format("Image format error on line %d",
                lineNumber));
          }
          lineNumber++;
       }
    }

    public List<PImage> getImageList(String key)
    {
       return this.images.getOrDefault(key, this.defaultImages);
    }
}
