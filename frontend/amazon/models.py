from django.db import models
from django.contrib.auth.models import User

class Category(models.Model):
    category = models.CharField(max_length=50)

class Product(models.Model):
    name = models.CharField(max_length=100)
    category = models.ForeignKey(Category, on_delete=models.CASCADE)
    price = models.DecimalField(decimal_places=2)

class Warehouse(models.Model):
    location_x = models.IntegerField()
    location_y = models.IntegerField()

class Package(models.Model):
    owner = models.ForeignKey(User, on_delete=models.CASCADE, null=True)
    warehouse = models.ForeignKey(Warehouse, on_delete=models.CASCADE)
    destination_x = models.IntegerField()
    destination_y = models.IntegerField()
    truck_id = models.IntegerField(default=-1)
    ups_account = models.CharField(max_length=50, null=True)
    ups_verified = models.BooleanField(default=False)
    status = models.CharField(max_length=50, default='purchasing')
    # all possible status: purchasing, purchased, packing, packed, loading, loaded, delivering, delivered

class Item(models.Model):
    buyer = models.ForeignKey(User, on_delete=models.CASCADE)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    product_num = models.PositiveIntegerField()
    package = models.ForeignKey(Package, on_delete=models.CASCADE)

    def __str__(self):
        return '%s'%(self.name)
