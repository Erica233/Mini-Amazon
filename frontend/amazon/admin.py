from django.contrib import admin
from .models import Item, Category, Product, Warehouse, Package

admin.site.register(Item, Category, Product, Warehouse, Package)