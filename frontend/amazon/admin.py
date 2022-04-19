from django.contrib import admin
from .models import Item, Category, Product, Warehouse, Package

admin.site.register(Item)
admin.site.register(Category)
admin.site.register(Product)
admin.site.register(Warehouse)
admin.site.register(Package)