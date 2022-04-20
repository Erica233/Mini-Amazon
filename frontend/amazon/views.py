from django.contrib import messages
from django.contrib.auth.decorators import login_required
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.urls import reverse

from .models import Item, Product, Category, Package, Warehouse


#from django.views.generic import ListView

def home(request):
    context = {
        'categories': Category.objects.all()
    }
    return render(request, 'amazon/home.html', context)

def about(request):
    return render(request, 'amazon/about.html', {'title': 'About'})

def allProducts(request):
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'products': Product.objects.all(),
        'curr_nav': 'all'
    }
    return render(request, 'amazon/categories.html', context)

def categories(request, a_category):
    cat = Category.objects.get(category=a_category)
    products = Product.objects.filter(category_id=cat.id)
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'products': products,
        'curr_nav': a_category
    }
    return render(request, 'amazon/categories.html', context)

@login_required
def oneProduct(request, a_product):
    product = Product.objects.get(name=a_product)
    curr_cat = product.category.category

    if request.method == "POST":
        product_num = request.POST['product_num']
        destination_x = request.POST.get('destination_x', False)
        destination_y = request.POST.get('destination_y', False)
        ups_account = request.POST.get('destination_y', False)
        warehouse = Warehouse.objects.get(id=1)
        package = Package.objects.create(owner=request.user, warehouse=warehouse, destination_x=destination_x,
                                         destination_y=destination_y, ups_account=ups_account)
        item = Item.objects.create(buyer=request.user, product=product, product_num=product_num, package=package)
        messages.add_message(request, messages.INFO, 'Order Created Successfully!')
        return HttpResponseRedirect(reverse('amazon-products'))
    else:
        context = {
            'categories': Category.objects.all().order_by('-category'),
            'product': product,
            'curr_nav': curr_cat
        }
        return render(request, 'amazon/product.html', context)