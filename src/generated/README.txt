The files under this directory should **not** be edited by hand, and any PR's trying to do so
will be closed without warning. Please see the twilightforest.data package and edit the appropriate datagenerator,
then run the `runDatagen` gradle task to regenerate these files.

Basically: src/main/resources = hand-edited, src/generated/resources = `runDatagen`-generated.

FIXME: fix errors at the end of datagen runs on fabric (these errors don't effect the generation of sources, but are
FIXME: datagen doesn't generate some files (uncrafting_recipe)