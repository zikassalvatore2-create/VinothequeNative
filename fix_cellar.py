import os

path = r'c:\Users\zakar\Downloads\VinothequeNative\app\src\main\java\com\vinotheque\nativeapp\ui\CellarScreen.kt'

with open(path, 'rb') as f:
    content = f.read().decode('utf-8', errors='ignore')

old_text = '''            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { isListView = !isListView }) {
                Icon(if (isListView) Icons.Default.GridView else Icons.Default.ViewList, "Toggle View", tint = WineGold)
            }'''

new_text = '''            if (isAdmin) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { isListView = !isListView }) {
                    Icon(if (isListView) Icons.Default.GridView else Icons.Default.ViewList, "Toggle View", tint = WineGold)
                }
            }'''

if old_text in content:
    new_content = content.replace(old_text, new_text)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Successfully updated CellarScreen.kt")
else:
    print("Could not find target text in CellarScreen.kt")
