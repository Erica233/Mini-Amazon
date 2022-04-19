from django.db import models
from django.contrib.auth.models import User

"""class Product(models.Model):
    name = models.CharField(max_length=100)
    category = models.CharField(max_length=100)
    #category = models.ForeignKey(Category, on_delete=models.CASCADE)
    price = models.DecimalField(max_digits=3, decimal_places=1)"""

class Item(models.Model):
    buyer = models.ForeignKey(User, on_delete=models.CASCADE)
    #product = models.ForeignKey(Product, on_delete=models.CASCADE)
    name = models.CharField(max_length=100)
    product_num = models.PositiveIntegerField()
    price = models.DecimalField(max_digits=10, decimal_places=2)
    #package = models.ForeignKey(Package, on_delete=models.CASCADE)

    def __str__(self):
        return '%s'%(self.name)


