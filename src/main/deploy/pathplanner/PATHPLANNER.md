# PathPlanner project files

This folder is the PathPlanner project. It holds robot geometry and constraints in `settings.json`, the pathfinding map in `navgrid.json`, and authored paths in `paths/`.

Update settings and regenerate the navgrid after real robot geometry or official field changes. If pathfinding is poor or routes collide with field geometry, verify the field origin, robot dimensions, obstacles, and navgrid before changing autonomous code. Assemble routines in Java; the team does not use `.auto` files.
