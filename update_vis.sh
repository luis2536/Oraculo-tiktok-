#!/bin/bash
sed -i 's/val levitationOffset = sin(magicPhase \* 2f).toFloat() \* 12.dp.toPx()/val levitationOffset = sin(magicPhase * 2f).toFloat() * 12f/' app/src/main/java/com/example/ui/components/MysticOracleVisualizer.kt
sed -i 's/R.drawable.cyber_oracle/R.drawable.img_oracle_character/' app/src/main/java/com/example/ui/components/MysticOracleVisualizer.kt
# Remove Particles
sed -i '/Particles(/,/scale = scale/d' app/src/main/java/com/example/ui/components/MysticOracleVisualizer.kt
sed -i 's/modifier = Modifier.fillMaxSize(),//' app/src/main/java/com/example/ui/components/MysticOracleVisualizer.kt
