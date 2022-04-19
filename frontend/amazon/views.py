from django.shortcuts import render
from django.http import HttpResponse
from .models import Item, Product
#from django.views.generic import ListView

def home(request):
    context = {
        'products': Product.objects.all()
    }
    return render(request, 'amazon/home.html', context)

def about(request):
    return render(request, 'amazon/about.html', {'title': 'About'})

'''class ProductListView(ListView):
    model = Item'''
