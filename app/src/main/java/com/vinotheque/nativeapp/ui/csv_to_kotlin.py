import csv

input_file = r'c:\Users\zakar\Desktop\ratings.csv'

wines = []
with open(input_file, mode='r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for i, row in enumerate(reader):
        if i >= 40: break
        
        ref = row.get('Reference', f'SAM{i:03}')
        name = row.get('Name', '').replace('"', '\\"')
        vintage = row.get('Vintage', '')
        grape = row.get('grape_variety', '')
        aroma = row.get('tasting_notes', '').replace('"', '\\"')
        glass = row.get('glass_type', '')
        decanting = row.get('decanting_recommended', '')
        temp = row.get('serving_temp', '')
        pairing = row.get('food_pairing', '').replace('"', '\\"')
        price = row.get('Price', '0.0')
        rating = row.get('rating', '0').split('.')[0]
        source = row.get('rating_source', '').replace('"', '\\"')
        
        wine_type = "Red"
        g_low = grape.lower()
        n_low = name.lower()
        if any(x in g_low or x in n_low for x in ["chardonnay", "white", "blanc", "riesling", "gruner"]):
            wine_type = "White"
        elif "rose" in n_low:
            wine_type = "Rose"
        elif any(x in n_low for x in ["champagne", "sparkling", "brut"]):
            wine_type = "Sparkling"
        elif "semillon" in g_low or "sweet" in n_low:
            wine_type = "Dessert"
        
        wine_line = f'Wine("{ref}", "{name}", "Sample Collection", "{vintage}", "{grape}", "{wine_type}", "Dry", {price}, {rating}, "{aroma}", null, "{pairing}", "", "Bin-{i+1}", 0, "{glass}", "", "{decanting}", "{temp}", "{source}", "")'
        wines.append(wine_line)

print(",\n".join(wines))
