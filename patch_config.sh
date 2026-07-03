#!/bin/bash
sed -i '/OutlinedTextField(/,/modifier = Modifier.fillMaxWidth()/d' app/src/main/java/com/example/ui/components/ConfigDialog.kt
