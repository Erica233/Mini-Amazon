from django.db import models

class Warehouse(models.Model):
    location_x = models.IntegerField()
    location_y = models.IntegerField()

class Category(models.Model):
    category = models.CharField(max_length=50)

class Product(models.Model):
    name = models.CharField(max_length=100)
    category = models.ForeignKey(Category, on_delete=models.CASCADE)
    price = models.DecimalField(max_digits=3, decimal_places=1)
    description = models.CharField(max_length=250)

class Package(models.Model):
    warehouse = models.ForeignKey(Warehouse, on_delete=models.CASCADE)
    destination_x = models.IntegerField()
    destination_y = models.IntegerField()
    status = models.CharField(max_length=50)

class Order(models.Model):
    buyer = models.CharField(max_length=50)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    product_num = models.IntegerField()
    package = models.ForeignKey(Package, on_delete=models.CASCADE)