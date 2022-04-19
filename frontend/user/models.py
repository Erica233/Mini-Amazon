from django.db import models
from django.contrib.auth.models import User

class MyInfo(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    upsAccount = models.CharField(max_length=50, blank=True,default='')

    def __str__(self):
        return '%s_%s'%(self.user.username, self.upsAccount)
