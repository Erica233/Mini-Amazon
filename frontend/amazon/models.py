from django.db import models

class User(models.Model):
    username = models.CharField(max_length=50)
    password = models.CharField(max_length=50)
    email = models.CharField(max_length=50)

class Warehouse(models.Model):
    location_x = models.IntegerField()
    location_y = models.IntegerField()

class Category(models.Model):
    category = models.CharField(max_length=50)

class Product(models.Model):
    name = models.CharField(max_length=100)
    category = models.ForeignKey(Category, on_delete=models.CASCADE)
    price = models.DecimalField(max_digits=3, decimal_places=1)

class Package(models.Model):
    owner = models.ForeignKey(User, on_delete=models.CASCADE, null=True)
    warehouse = models.ForeignKey(Warehouse, on_delete=models.CASCADE)
    destination_x = models.IntegerField()
    destination_y = models.IntegerField()
    truck_id = models.IntegerField(default=-1)
    ups_account = models.CharField(max_length=50, null=True)
    status = models.CharField(max_length=50, default='purchasing')
    # all possible status: purchasing, purchased, packing, packed, loading, loaded, delivering, delivered

class Item(models.Model):
    buyer = models.ForeignKey(User, on_delete=models.CASCADE)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    product_num = models.IntegerField()
    package = models.ForeignKey(Package, on_delete=models.CASCADE)
