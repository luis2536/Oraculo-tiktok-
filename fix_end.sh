#!/bin/bash
# Remove last 2 lines
head -n -2 app/src/main/java/com/example/ui/components/MysticOracleVisualizer.kt > tmp.kt
echo "        }" >> tmp.kt
echo "    }" >> tmp.kt
echo "}" >> tmp.kt
mv tmp.kt app/src/main/java/com/example/ui/components/MysticOracleVisualizer.kt
