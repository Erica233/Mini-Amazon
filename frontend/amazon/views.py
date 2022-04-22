import socket
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

def allProducts(request):
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'products': Product.objects.all(),
        'curr_nav': 'all'
    }
    return render(request, 'amazon/categories.html', context)

def categories(request, a_category):
    try:
        cat = Category.objects.get(category=a_category)
    except Category.DoesNotExist:
        messages.add_message(request, messages.ERROR, 'This category does not exist!')
        return HttpResponseRedirect(reverse('amazon-products'))
    products = Product.objects.filter(category_id=cat.id)
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'products': products,
        'curr_nav': a_category
    }
    return render(request, 'amazon/categories.html', context)

@login_required
def oneProduct(request, a_product):
    try:
        product = Product.objects.get(name=a_product)
    except Product.DoesNotExist:
        messages.add_message(request, messages.ERROR, 'This product does not exist!')
        return HttpResponseRedirect(reverse('amazon-products'))
    curr_cat = product.category.category

    if request.method == "POST":
        product_num = request.POST['product_num']
        destination_x = request.POST['destination_x']
        destination_y = request.POST['destination_y']
        ups_account = request.POST.get('ups_account', '')
        ups_verified = False
        # allocate the nearest warehouse location
        warehouses = Warehouse.objects.all()
        min_dist = 10000
        warehouse = Warehouse.objects.get(id=1)
        for wh in warehouses.iterator():
            dist = abs(wh.location_x - int(destination_x)) ** 2 + abs(wh.location_y - int(destination_y)) ** 2
            if min_dist > dist:
                min_dist = dist
                warehouse = wh
        package_price = product.price * int(product_num)
        package = Package.objects.create(owner=request.user, warehouse=warehouse, destination_x=destination_x,
                                         destination_y=destination_y, ups_account=ups_account, ups_verified=ups_verified,
                                         package_price=package_price)
        item = Item.objects.create(buyer=request.user, product=product, product_num=product_num, package=package)
        # send package_id to backend through TCP socket
        #s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        #s.connect(("www.python.org", 5678))
        #s.close()
        messages.add_message(request, messages.INFO, 'Order Created Successfully!')
        return HttpResponseRedirect(reverse('amazon-products'))
    else:
        context = {
            'categories': Category.objects.all().order_by('-category'),
            'product': product,
            'curr_nav': curr_cat
        }
        return render(request, 'amazon/product.html', context)

@login_required
def orders(request):
    items = Item.objects.filter(buyer=request.user)
    packages = Package.objects.filter(owner=request.user).order_by('-create_time')
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'items': items,
        'packages': packages,
        'curr_nav': 'all'
    }
    return render(request, 'amazon/orders.html', context)

@login_required
def oneOrder(request, package_id):
    try:
        package = Package.objects.get(id=package_id)
    except Package.DoesNotExist:
        messages.add_message(request, messages.ERROR, 'This order does not exist!')
        return HttpResponseRedirect(reverse('amazon-orders'))
    if package.owner != request.user:
        messages.add_message(request, messages.ERROR, 'You do not have this order!')
        return HttpResponseRedirect(reverse('amazon-orders'))
    items = package.items.all()
    context = {
        'items': items,
        'package': package
    }
    return render(request, 'amazon/oneOrder.html', context)