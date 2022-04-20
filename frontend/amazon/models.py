from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone

class Category(models.Model):
    category = models.CharField(max_length=50)

    def __str__(self):
        return '%s'%(self.category)

class Product(models.Model):
    name = models.CharField(max_length=100)
    category = models.ForeignKey(Category, on_delete=models.CASCADE)
    price = models.DecimalField(decimal_places=2, max_digits=7)

    def __str__(self):
        return '%s'%(self.name)

class Warehouse(models.Model):
    location_x = models.IntegerField()
    location_y = models.IntegerField()

    def __str__(self):
        return '(%s, %s)'%(self.location_x, self.location_y)

class Package(models.Model):
    owner = models.ForeignKey(User, on_delete=models.CASCADE, null=True)
    warehouse = models.ForeignKey(Warehouse, on_delete=models.CASCADE)
    destination_x = models.IntegerField()
    destination_y = models.IntegerField()
    truck_id = models.IntegerField(default=-1)
    ups_account = models.CharField(max_length=50, null=True, blank=True)
    ups_verified = models.BooleanField(default=False)
    status = models.CharField(max_length=50, default='purchasing')
    create_time = models.DateTimeField(default=timezone.now)
    # all possible status: purchasing, purchased, packing, packed, loading, loaded, delivering, delivered

    def __str__(self):
        return '%s'%(self.owner.username)

class Item(models.Model):
    buyer = models.ForeignKey(User, on_delete=models.CASCADE)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    product_num = models.PositiveIntegerField(default=1)
    package = models.ForeignKey(Package, on_delete=models.CASCADE)

    def __str__(self):
        return '%s_%s'%(self.product.name, self.buyer.username)
